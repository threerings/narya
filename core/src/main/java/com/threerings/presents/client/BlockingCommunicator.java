//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.presents.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import com.samskivert.util.LoopingThread;
import com.samskivert.util.Queue;
import com.samskivert.util.StringUtil;
import com.samskivert.util.Throttle;
import com.threerings.io.ByteBufferInputStream;
import com.threerings.io.ByteBufferOutputStream;
import com.threerings.io.FramedInputStream;
import com.threerings.io.FramingOutputStream;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.UnreliableObjectInputStream;
import com.threerings.io.UnreliableObjectOutputStream;
import com.threerings.presents.net.AESAuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.DownstreamMessage;
import com.threerings.presents.net.LogoffRequest;
import com.threerings.presents.net.PingRequest;
import com.threerings.presents.net.PublicKeyCredentials;
import com.threerings.presents.net.SecureRequest;
import com.threerings.presents.net.SecureResponse;
import com.threerings.presents.net.TransmitDatagramsRequest;
import com.threerings.presents.net.Transport;
import com.threerings.presents.net.UpstreamMessage;
import com.threerings.presents.util.DatagramSequencer;

import static com.threerings.presents.Log.log;

/**
 * The client performs all network I/O on separate threads (one for reading and one for
 * writing). The communicator class encapsulates that functionality.
 *
 * <pre>
 * Logon synopsis:
 *
 * Client.logon():
 * - Calls Communicator.start()
 * Communicator.start():
 * - spawn Reader thread
 * Reader.run():
 * { - connect
 *   - authenticate
 * } if either fail, notify observers of failed logon
 * - start writer thread
 * - notify observers that we're logged on
 * - read loop
 * Writer.run():
 * - write loop
 * </pre>
 */
public class BlockingCommunicator extends Communicator
{
    /**
     * Creates a new communicator instance which is associated with the supplied client.
     */
    public BlockingCommunicator (Client client)
    {
        super(client);
    }

    @Override // from Communicator
    public void logon ()
    {
        // make sure things are copacetic
        if (_reader != null) {
            throw new RuntimeException("Communicator already started.");
        }

        // start up the reader thread. it will connect to the server and start up the writer thread
        // if everything went successfully
        _reader = new Reader();
        _reader.start();
    }

    @Override // from Communicator
    public synchronized void logoff ()
    {
        // if our socket is already closed, we've already taken care of this business
        if (_channel == null) {
            return;
        }

        // post a logoff message
        postMessage(new LogoffRequest());

        // let our readers and writers know that it's time to go
        if (_reader != null) {
            // if logoff() is being called by the client as part of a normal shutdown, this will
            // cause the reader thread to be interrupted and shutdown gracefully. if logoff is
            // being called by the reader thread as a result of a failed socket, it won't interrupt
            // itself as it is already shutting down gracefully. if the JVM is buggy and calling
            // interrupt() on a thread that is blocked on a socket doesn't wake it up, then when we
            // close() the socket a bit further down, we have another chance that the reader thread
            // will wake up; this time slightly less gracefully because it will think there's a
            // network error when in fact we're just shutting down, but at least it will cleanly
            // exit
            _reader.shutdown();
        }
        if (_writer != null) {
            // shutting down the writer thread is simpler because we can post a termination message
            // on the queue and be sure that it will receive it. when the writer thread has
            // delivered our logoff request and exited, we will complete the logoff process by
            // closing our socket and invoking the clientDidLogoff callback
            _writer.shutdown();
        }
        if (_datagramWriter != null) {
            _datagramWriter.shutdown();
        }
        if (_datagramReader != null) {
            _datagramReader.shutdown();
        }
    }

    @Override // from Communicator
    public void gotBootstrap ()
    {
        // start the datagram writer thread, if applicable
        if (_client.getDatagramPorts().length > 0) {
            _datagramReader = new DatagramReader();
            _datagramReader.start();
        }
    }

    @Override // from Communicator
    public void postMessage (UpstreamMessage msg)
    {
        // post as datagram if hinted and possible
        if (!msg.getTransport().isReliable() && _datagramWriter != null) {
            msg.noteActualTransport(Transport.UNRELIABLE_UNORDERED);
            _dataq.append(msg);
        } else {
            msg.noteActualTransport(Transport.RELIABLE_ORDERED);
            _msgq.append(msg);
        }
    }

    @Override // from Communicator
    public void setClassLoader (ClassLoader loader)
    {
        _loader = loader;
        if (_oin != null) {
            _oin.setClassLoader(loader);
        }
    }

    @Override // from Communicator
    public synchronized long getLastWrite ()
    {
        return _lastWrite;
    }

    @Override // from Communicator
    public boolean getTransmitDatagrams ()
    {
        return _datagramWriter != null;
    }

    @Override // from Communicator
    protected synchronized void logonSucceeded (AuthResponseData data)
    {
        super.logonSucceeded(data);

        // create a new writer thread and start it up
        if (_writer != null) {
            throw new RuntimeException("Writer already started!?");
        }
        _writer = new Writer();
        _writer.start();
    }

    /**
     * Callback called by the reader or writer thread when something goes awry with our socket
     * connection to the server.
     */
    protected synchronized void connectionFailed (final IOException ioe)
    {
        // make sure the socket isn't already closed down (meaning we've already dealt with the
        // failed connection)
        if (_channel == null) {
            return;
        }

        log.info("Connection failed", ioe);

        // let the client know that things went south
        notifyClientObservers(new ObserverOps.Client(_client) {
            @Override protected void notify (ClientObserver obs) {
                obs.clientConnectionFailed(_client, ioe);
            }
        });

        // and request that we go through the motions of logging off
        logoff();
    }

    /**
     * Callback called by the reader if the server closes the other end of the connection.
     */
    protected synchronized void connectionClosed ()
    {
        // make sure the socket isn't already closed down (meaning we've already dealt with the
        // closed connection)
        if (_channel == null) {
            return;
        }

        log.debug("Connection closed.");
        // now do the whole logoff thing
        logoff();
    }

    /**
     * Callback called by the reader thread when it goes away.
     */
    protected synchronized void readerDidExit ()
    {
        // clear out our reader reference
        _reader = null;

        if (_writer == null) {
            // there's no writer during authentication, so we may be responsible for closing the
            // socket channel
            closeChannel();

            // let the client know when we finally go away
            clientCleanup(_logonError);
        }

        log.debug("Reader thread exited.");
    }

    /**
     * Callback called by the writer thread when it goes away.
     */
    protected synchronized void writerDidExit ()
    {
        // clear out our writer reference
        _writer = null;
        log.debug("Writer thread exited.");

        // let the client observers know that we're logged off
        notifyClientObservers(new ObserverOps.Session(_client) {
            @Override protected void notify (SessionObserver obs) {
                obs.clientDidLogoff(_client);
            }
        });

        // now that the writer thread has gone away, we can safely close our socket and let the
        // client know that the logoff process has completed
        closeChannel();

        // let the client know when we finally go away
        if (_reader == null) {
            clientCleanup(_logonError);
        }
    }

    /**
     * Closes the socket channel that we have open to the server. Called by either {@link
     * #readerDidExit} or {@link #writerDidExit} whichever is called last.
     */
    protected void closeChannel ()
    {
        if (_channel != null) {
            log.debug("Closing socket channel.");

            try {
                _channel.close();
            } catch (IOException ioe) {
                log.warning("Error closing failed socket: " + ioe);
            }
            _channel = null;

            // clear these out because they are probably large and in charge
            _oin = null;
            _oout = null;
        }
    }

    /**
     * Callback called by the datagram reader thread when it goes away.
     */
    protected synchronized void datagramReaderDidExit ()
    {
        // clear out our reader reference
        _datagramReader = null;

        if (_datagramWriter == null) {
            closeDatagramChannel();
        }

        log.debug("Datagram reader thread exited.");
    }

    /**
     * Callback called by the datagram writer thread when it goes away.
     */
    protected synchronized void datagramWriterDidExit ()
    {
        // clear out our writer reference
        _datagramWriter = null;

        if (_datagramReader == null) {
            closeDatagramChannel();
        }

        log.debug("Datagram writer thread exited.");
    }

    /**
     * Closes the datagram channel.
     */
    protected void closeDatagramChannel ()
    {
        if (_selector != null) {
            try {
                _selector.close();
            } catch (IOException ioe) {
                log.warning("Error closing selector: " + ioe);
            }
            _selector = null;
        }
        if (_datagramChannel != null) {
            log.debug("Closing datagram socket channel.");

            try {
                _datagramChannel.close();
            } catch (IOException ioe) {
                log.warning("Error closing datagram socket: " + ioe);
            }
            _datagramChannel = null;

            // clear these out because they are probably large and in charge
            _uout = null;
            _sequencer = null;
        }
    }

    /**
     * Writes the supplied message to the socket.
     */
    protected void sendMessage (UpstreamMessage msg)
        throws IOException
    {
        if (debugLogMessages()) {
            log.info("SEND " + msg);
        }

        // first we write the message so that we can measure it's length
        _oout.writeObject(msg);
        _oout.flush();

        // then write the framed message to actual output stream
        try {
            ByteBuffer buffer = _fout.frameAndReturnBuffer();
            if (buffer.limit() > 4096) {
                String txt = StringUtil.truncate(String.valueOf(msg), 80, "...");
                log.info("Whoa, writin' a big one", "msg", txt, "size", buffer.limit());
            }
            int wrote = writeMessage(buffer);
            if (wrote != buffer.limit()) {
                log.warning("Aiya! Couldn't write entire message", "msg", msg,
                            "size", buffer.limit(), "wrote", wrote);
            } else {
//                 Log.info("Wrote " + wrote + " bytes.");
                _client.getMessageTracker().messageSent(false, wrote, msg);
            }

        } finally {
            _fout.resetFrame();
        }

        // make a note of our most recent write time
        updateWriteStamp();
    }

    /**
     * Writes the message contained in the supplied buffer.
     *
     * @return the number of bytes written.
     */
    protected int writeMessage (ByteBuffer buf)
        throws IOException
    {
        return _channel.write(buf);
    }

    /**
     * Sends a datagram over the datagram socket.
     */
    protected void sendDatagram (UpstreamMessage msg)
        throws IOException
    {
        // reset the stream and write our connection id and hash placeholder
        _bout.reset();
        _uout.writeInt(_client.getConnectionId());
        _uout.writeLong(0L);

        // write the datagram through the sequencer
        _sequencer.writeDatagram(msg);

        // flip the buffer and make sure it's not too long
        ByteBuffer buf = _bout.flip();
        int size = buf.remaining();
        if (size > Client.MAX_DATAGRAM_SIZE) {
            log.warning("Dropping oversized datagram", "size", size, "msg", msg);
            return;
        }

        // compute the hash
        buf.position(12);
        _digest.update(buf);
        byte[] hash = _digest.digest(_secret);

        // insert the first 64 bits of the hash
        buf.position(4);
        buf.put(hash, 0, 8).rewind();

        // send the datagram
        writeDatagram(buf);

        // notify the tracker
        _client.getMessageTracker().messageSent(true, size, msg);
    }

    /**
     * Writes the datagram contained in the supplied buffer.
     *
     * @return the number of bytes written.
     */
    protected int writeDatagram (ByteBuffer buf)
        throws IOException
    {
        return _datagramChannel.write(buf);
    }

    /**
     * Reads a new message from the socket (blocking until a message has arrived).
     */
    protected DownstreamMessage receiveMessage ()
        throws IOException
    {
        // read in the next message frame (readFrame() can return false meaning it only read part
        // of the frame from the network, in which case we simply call it again because we can't do
        // anything until it has a whole frame; it will throw an exception if it hits EOF or if
        // something goes awry)
        while (!readFrame()) {
            // noop!
        }

        if (_oin == null) {
            // Our object input stream was taken away from us before we could do anything with the
            //  frame, this is equivalent to being interrupted mid-frame, so indicate that.
            throw new InterruptedIOException();
        }

        try {
            int size = _fin.available();
            DownstreamMessage msg = (DownstreamMessage)_oin.readObject();
            if (debugLogMessages()) {
                log.info("RECEIVE " + msg);
            }
            _client.getMessageTracker().messageReceived(false, size, msg, 0);
            return msg;

        } catch (ClassNotFoundException cnfe) {
            throw (IOException) new IOException(
                "Unable to decode incoming message.").initCause(cnfe);
        }
    }

    /**
     * Reads a frame from the socket.
     *
     * @return true if a complete frame is available, false if more needs to be read.
     */
    protected boolean readFrame ()
        throws IOException
    {
        return _fin.readFrame(_channel);
    }

    /**
     * Reads a datagram from the socket (blocking until a datagram has arrived).
     */
    protected DownstreamMessage receiveDatagram ()
        throws IOException
    {
        // clear the buffer and read a datagram
        _buf.clear();
        int size = readDatagram(_buf);
        if (size <= 0) {
            throw new IOException("No datagram available to read.");
        }
        _buf.flip();

        // decode through the sequencer
        try {
            DownstreamMessage msg = (DownstreamMessage)_sequencer.readDatagram();
            if (_client != null) {
                _client.getMessageTracker().messageReceived(
                    true, size, msg, (msg == null) ? 0 : _sequencer.getMissedCount());
            }
            if (msg == null) {
                return null; // received out of order
            }
            if (debugLogMessages()) {
                log.info("DATAGRAM " + msg);
            }
            return msg;

        } catch (ClassNotFoundException cnfe) {
            throw (IOException) new IOException(
                "Unable to decode incoming datagram.").initCause(cnfe);
        }
    }

    /**
     * Reads a datagram into the supplied buffer.
     *
     * @return the number of bytes read.
     */
    protected int readDatagram (ByteBuffer buf)
        throws IOException
    {
        return _datagramChannel.read(buf);
    }

    protected void openChannel (InetAddress host)
        throws IOException
    {
        // the default implementation just connects to the first port and does no cycling
        int port = _client.getPorts()[0];
        log.info("Connecting", "host", host, "port", port);
        synchronized (BlockingCommunicator.this) {
            _channel = SocketChannel.open(new InetSocketAddress(host, port));
        }
    }

    protected boolean debugLogMessages ()
    {
        return false;
    }

    /**
     * Throttles an outgoing message operation in a thread-safe manner.
     */
    protected void throttleOutgoingMessage ()
    {
        Throttle throttle = _client.getOutgoingMessageThrottle();
        synchronized(throttle) {
            while (throttle.throttleOp()) {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException ie) {
                    // no problem
                }
            }
        }
    }

    /**
     * The reader encapsulates the authentication and message reading process. It calls back to the
     * {@link Communicator} class to do things, but the general flow of the reader thread is
     * encapsulated in this class.
     */
    protected class Reader extends LoopingThread
    {
        public Reader () {
            super("BlockingCommunicator_Reader");
        }

        @Override
        protected void willStart () {
            try {
                // connect to the server
                connect();

                // If a public key is specified, we'll attempt to establish a secure authentication
                // channel
                PublicKey key = _client.getPublicKey();
                AuthResponse response = null;
                if (key != null) {
                    PublicKeyCredentials pkcreds = new PublicKeyCredentials(key);
                    sendMessage(new SecureRequest(pkcreds, _client.getVersion()));

                    // now wait for the handshake
                    log.debug("Waiting for secure response.");

                    response = (AuthResponse)receiveMessage();
                    // If we've received a secure response, proceed with authentication
                    if (response instanceof SecureResponse) {
                        AuthRequest areq = AESAuthRequest.createAuthRequest(
                                    _client.getCredentials(), _client.getVersion(),
                                    _client.getBootGroups(), _client.requireSecureAuth(),
                                    pkcreds, (SecureResponse)response);
                        sendMessage(areq);
                        _client.setSecret(areq.getSecret());

                        // now wait for the auth response
                        log.debug("Waiting for auth response.");
                        response = (AuthResponse)receiveMessage();
                    }

                } else {
                    // construct an auth request and send it
                    sendMessage(AESAuthRequest.createAuthRequest(
                                _client.getCredentials(), _client.getVersion(),
                                _client.getBootGroups(), _client.requireSecureAuth()));


                    // now wait for the auth response
                    log.debug("Waiting for auth response.");
                    response = (AuthResponse)receiveMessage();
                }
                gotAuthResponse(response);


            } catch (Exception e) {
                log.debug("Logon failed: " + e);
                // once we're shutdown we'll report this error
                _logonError = e;
                // terminate our communicator thread
                shutdown();
            }
        }

        protected void connect ()
            throws IOException
        {
            // if we're already connected, we freak out
            if (_channel != null) {
                throw new IOException("Already connected.");
            }

            // look up the address of the target server
            InetAddress host = InetAddress.getByName(_client.getHostname());
            openChannel(host);
            _channel.configureBlocking(true);

            // our messages are framed (preceded by their length), so we use these helper streams
            // to manage the framing
            _fin = new FramedInputStream();
            _fout = new FramingOutputStream();

            // create our object input and output streams
            _oin = new ClientObjectInputStream(_client, _fin);
            _oin.setClassLoader(_loader);
            _oout = new ObjectOutputStream(_fout);
        }

        // now that we're authenticated, we manage the reading half of things by continuously
        // reading messages from the socket and processing them
        @Override
        protected void iterate () {
            DownstreamMessage msg = null;

            try {
                // read the next message from the socket
                msg = receiveMessage();

                // process the message
                processMessage(msg);

            } catch (InterruptedIOException iioe) {
                // somebody set up us the bomb! we've been interrupted which means that we're being
                // shut down, so we just report it and return from iterate() like a good monkey
                log.debug("Reader thread woken up in time to die.");

            } catch (EOFException eofe) {
                // let the communicator know that our connection was closed
                connectionClosed();
                // and shut ourselves down
                shutdown();

            } catch (IOException ioe) {
                // let the communicator know that our connection failed
                connectionFailed(ioe);
                // and shut ourselves down
                shutdown();

            } catch (Exception e) {
                log.warning("Error processing message", "msg", msg, e);
            }
        }

        @Override
        protected void handleIterateFailure (Exception e) {
            log.warning("Uncaught exception it reader thread.", e);
        }

        @Override
        protected void didShutdown () {
            // let the communicator know when we finally go away
            readerDidExit();
        }

        @Override
        protected void kick () {
            // we want to interrupt the reader thread as it may be blocked listening to the socket;
            // this is only called if the reader thread doesn't shut itself down

            // While it would be nice to be able to handle wacky cases requiring reader-side
            // shutdown, doing so causes consternation on the other end's writer which suddenly
            // loses its connection.  So, we rely on the writer side to take us down.
            // interrupt();
        }
    }

    /**
     * The writer encapsulates the message writing process. It calls back to the {@link
     * Communicator} class to do things, but the general flow of the writer thread is encapsulated
     * in this class.
     */
    protected class Writer extends LoopingThread
    {
        public Writer () {
            super("BlockingCommunicator_Writer");
        }

        @Override
        public synchronized void shutdown () {
            // we want to finish off what's in our queue before we actually shutdown
            postMessage(new TerminationMessage());
        }

        @Override
        protected void iterate () {
            // fetch the next message from the queue
            UpstreamMessage msg = _msgq.get();

            // if this is a termination message, we're being requested to exit, so we call
            // super.shutdown() to mark ourselves as not running and then return
            if (msg instanceof TerminationMessage) {
                super.shutdown();
                return;
            }

            // make sure we're not exceeding our outgoing throttle rate
            throttleOutgoingMessage();

            try {
                // write the message out the socket
                sendMessage(msg);

            } catch (IOException ioe) {
                connectionFailed(ioe); // let the communicator know
                super.shutdown(); // and bail immediately (which is why we call super)
            }
        }

        @Override
        protected void handleIterateFailure (Exception e) {
            log.warning("Uncaught exception it writer thread.", e);
        }

        @Override
        protected void didShutdown () {
            writerDidExit();
        }
    }

    /**
     * Handles the general flow of reading datagrams.
     */
    protected class DatagramReader extends LoopingThread
    {
        public DatagramReader () {
            super("BlockingCommunicator_DatagramReader");
        }

        @Override
        protected void willStart () {
            try {
                connect();
            } catch (IOException ioe) {
                log.warning("Failed to open datagram channel", "error", ioe);
                shutdown();
            }
        }

        protected void connect ()
            throws IOException
        {
            // create a selector to be used only for the initial connection
            _selector = Selector.open();

            // create and register the channel
            _datagramChannel = DatagramChannel.open();
            _datagramChannel.socket().setTrafficClass(0x10); // IPTOS_LOWDELAY
            _datagramChannel.configureBlocking(false);
            _datagramChannel.register(_selector, SelectionKey.OP_READ, null);

            // create the message digest
            try {
                _digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException nsae) {
                log.warning("Missing MD5 algorithm.");
                shutdown();
                return;
            }
            _secret = _client.getCredentials().getDatagramSecret().getBytes("UTF-8");

            // create our various streams
            _bout = new ByteBufferOutputStream();
            _uout = new UnreliableObjectOutputStream(_bout);
            ByteBufferInputStream bin = new ByteBufferInputStream(_buf);
            UnreliableObjectInputStream uin = new UnreliableObjectInputStream(bin);
            uin.setClassLoader(_loader);

            // create the datagram sequencer
            _sequencer = new DatagramSequencer(uin, _uout);

            // try each port in turn
            int cport = -1;
            for (int port : _client.getDatagramPorts()) {
                boolean connected = connect(port);
                if (!isRunning()) {
                    return; // cancelled

                } else if (connected) {
                    cport = port;
                    break;
                }
            }

            // close the selector and return the channel to blocking mode
            _selector.close();
            _selector = null;
            _datagramChannel.configureBlocking(true);

            // check if we managed to establish a connection
            if (cport > 0) {
                log.info("Datagram connection established", "port", cport);

                // notify the server
                postMessage(new TransmitDatagramsRequest());

                // start up the writer thread
                _datagramWriter = new DatagramWriter();
                _datagramWriter.start();

            } else {
                log.info("Failed to establish datagram connection.");
                shutdown();
            }
        }

        protected boolean connect (int port)
            throws IOException
        {
            _datagramChannel.connect(new InetSocketAddress(_client.getHostname(), port));
            for (int ii = 0; ii < DATAGRAM_ATTEMPTS_PER_PORT; ii++) {
                // send a ping datagram
                sendDatagram(new PingRequest(Transport.UNRELIABLE_UNORDERED));

                // wait for a response
                int resp = _selector.select(DATAGRAM_RESPONSE_WAIT);
                if (!isRunning()) {
                    return false; // cancelled

                } else if (resp > 0) {
                    receiveDatagram();
                    return true;
                }
            }
            _datagramChannel.disconnect();
            return false;
        }

        @Override
        protected void iterate () {
            DownstreamMessage msg = null;

            try {
                // read the next message from the socket
                msg = receiveDatagram();

                // process the message if it wasn't dropped
                if (msg != null) {
                    processMessage(msg);
                }

            } catch (AsynchronousCloseException ace) {
                // somebody set up us the bomb! we've been interrupted which means that we're being
                // shut down, so we just report it and return from iterate() like a good monkey
                log.debug("Datagram reader thread woken up in time to die.");

            } catch (IOException ioe) {
                log.warning("Error receiving datagram", ioe);

            } catch (Exception e) {
                log.warning("Error processing message", "msg", msg, e);
            }
        }

        @Override
        protected void handleIterateFailure (Exception e) {
            log.warning("Uncaught exception in datagram reader thread.", e);
        }

        @Override
        protected void didShutdown () {
            datagramReaderDidExit();
        }

        @Override
        protected void kick () {
            // if we have a selector, wake it up
            if (_selector != null) {
                _selector.wakeup();
            }
            // interrupt reading the current datagram
            interrupt();
        }
    }

    /**
     * Handles the general flow of writing datagrams.
     */
    protected class DatagramWriter extends LoopingThread
    {
        public DatagramWriter () {
            super("BlockingCommunicator_DatagramWriter");
        }

        @Override
        protected void iterate () {
            // fetch the next message from the queue
            UpstreamMessage msg = _dataq.get();

            // if this is a termination message, we're being requested to exit, so we want to bail
            // now rather than continuing
            if (msg instanceof TerminationMessage) {
                return;
            }

            // if we're exceeding our outgoing throttle rate, drop the packet
            Throttle throttle = _client.getOutgoingMessageThrottle();
            synchronized(throttle) {
                if (throttle.throttleOp()) {
                    return;
                }
            }

            try {
                // write the message out the socket
                sendDatagram(msg);

            } catch (IOException ioe) {
                log.warning("Error sending datagram", "error", ioe);
            }
        }

        @Override
        protected void handleIterateFailure (Exception e) {
            log.warning("Uncaught exception in datagram writer thread.", e);
        }

        @Override
        protected void didShutdown () {
            datagramWriterDidExit();
        }

        @Override
        protected void kick () {
            // post a bogus message to the outgoing queue to ensure that the writer thread notices
            // that it's time to go
            _dataq.append(new TerminationMessage());
        }
    }

    /** This is used to terminate the writer threads. */
    protected static class TerminationMessage extends UpstreamMessage
    {
    }

    protected Reader _reader;
    protected Writer _writer;

    protected DatagramReader _datagramReader;
    protected DatagramWriter _datagramWriter;

    protected SocketChannel _channel;
    protected Queue<UpstreamMessage> _msgq = new Queue<UpstreamMessage>();

    protected Selector _selector;
    protected DatagramChannel _datagramChannel;
    protected Queue<UpstreamMessage> _dataq = new Queue<UpstreamMessage>();

    protected Exception _logonError;

    /** We use this to frame our upstream messages. */
    protected FramingOutputStream _fout;
    protected ObjectOutputStream _oout;

    /** We use this to frame our downstream messages. */
    protected FramedInputStream _fin;
    protected ObjectInputStream _oin;

    /** We use these to write our upstream datagrams. */
    protected ByteBufferOutputStream _bout;
    protected UnreliableObjectOutputStream _uout;
    protected MessageDigest _digest;
    protected byte[] _secret;

    /** We use these to read our downstream datagrams. */
    protected ByteBuffer _buf = ByteBuffer.allocateDirect(Client.MAX_DATAGRAM_SIZE);

    protected DatagramSequencer _sequencer;

    protected ClassLoader _loader;

    /** The number of times per port to try to establish a datagram "connection". */
    protected static final int DATAGRAM_ATTEMPTS_PER_PORT = 10;

    /** The number of milliseconds to wait for a response datagram. */
    protected static final long DATAGRAM_RESPONSE_WAIT = 1000L;
}
