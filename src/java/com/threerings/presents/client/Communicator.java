//
// $Id: Communicator.java,v 1.34 2004/02/25 14:45:16 mdb Exp $

package com.threerings.presents.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.samskivert.io.NestableIOException;
import com.samskivert.util.LoopingThread;
import com.samskivert.util.Queue;
import com.samskivert.util.RuntimeAdjust;

import com.threerings.io.FramedInputStream;
import com.threerings.io.FramingOutputStream;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.net.DownstreamMessage;
import com.threerings.presents.net.LogoffRequest;
import com.threerings.presents.net.UpstreamMessage;

/**
 * The client performs all network I/O on separate threads (one for
 * reading and one for writing). The communicator class encapsulates that
 * functionality.
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
public class Communicator
{
    /**
     * Creates a new communicator instance which is associated with the
     * supplied client.
     */
    public Communicator (Client client)
    {
        _client = client;
    }

    /**
     * Returns the distributed object manager in effect for this session.
     * This instance is only valid while the client is connected to the
     * server.
     */
    public DObjectManager getDObjectManager ()
    {
        return _omgr;
    }

    /**
     * Logs on to the server and initiates our full-duplex message
     * exchange.
     */
    public void logon ()
    {
        // make sure things are copacetic
        if (_reader != null) {
            throw new RuntimeException("Communicator already started.");
        }

        // start up the reader thread. it will connect to the server and
        // start up the writer thread if everything went successfully
        _reader = new Reader();
        _reader.start();
    }

    /**
     * Delivers a logoff notification to the server and shuts down the
     * network connection. Also causes all communication threads to
     * terminate.
     */
    public synchronized void logoff ()
    {
        // if our socket is already closed, we've already taken care of
        // this business
        if (_channel == null) {
            return;
        }

        // post a logoff message
        postMessage(new LogoffRequest());

        // let our reader and writer know that it's time to go
        if (_reader != null) {
            // if logoff() is being called by the client as part of a
            // normal shutdown, this will cause the reader thread to be
            // interrupted and shutdown gracefully. if logoff is being
            // called by the reader thread as a result of a failed socket,
            // it won't interrupt itself as it is already shutting down
            // gracefully. if the JVM is buggy and calling interrupt() on
            // a thread that is blocked on a socket doesn't wake it up,
            // then when we close() the socket a bit further down, we have
            // another chance that the reader thread will wake up; this
            // time slightly less gracefully because it will think there's
            // a network error when in fact we're just shutting down, but
            // at least it will cleanly exit
            _reader.shutdown();
        }
        if (_writer != null) {
            // shutting down the writer thread is simpler because we can
            // post a termination message on the queue and be sure that it
            // will receive it. when the writer thread has delivered our
            // logoff request and exited, we will complete the logoff
            // process by closing our socket and invoking the
            // clientDidLogoff callback
            _writer.shutdown();
        }
    }

    /**
     * Queues up the specified message for delivery upstream.
     */
    public void postMessage (UpstreamMessage msg)
    {
        // simply append the message to the queue
        _msgq.append(msg);
    }

    /**
     * Callback called by the reader when the authentication process
     * completes successfully. Here we extract the bootstrap information
     * for the client and start up the writer thread to manage the other
     * half of our bi-directional message stream.
     */
    protected synchronized void logonSucceeded (AuthResponseData data)
    {
        Log.debug("Logon succeeded: " + data);

        // create our distributed object manager
        _omgr = new ClientDObjectMgr(this, _client);

        // create a new writer thread and start it up
        if (_writer != null) {
            throw new RuntimeException("Writer already started!?");
        }
        _writer = new Writer();
        _writer.start();

        // fill the auth data into the client's local field so that it can
        // be requested by external entities
        _client._authData = data;

        // wait for the bootstrap notification before we claim that we're
        // actually logged on
    }

    /**
     * Callback called by the reader or writer thread when something goes
     * awry with our socket connection to the server.
     */
    protected synchronized void connectionFailed (IOException ioe)
    {
        // make sure the socket isn't already closed down (meaning we've
        // already dealt with the failed connection)
        if (_channel == null) {
            return;
        }

        Log.info("Connection failed: " + ioe);
        Log.logStackTrace(ioe);

        // let the client know that things went south
        _client.notifyObservers(Client.CLIENT_CONNECTION_FAILED, ioe);

        // and request that we go through the motions of logging off
        logoff();
    }

    /**
     * Callback called by the reader if the server closes the other end of
     * the connection.
     */
    protected synchronized void connectionClosed ()
    {
        // make sure the socket isn't already closed down (meaning we've
        // already dealt with the closed connection)
        if (_channel == null) {
            return;
        }

        Log.debug("Connection closed.");
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
            // there's no writer during authentication, so we may be
            // responsible for closing the socket channel
            closeChannel();

            // let the client know when we finally go away
            _client.communicatorDidExit();
        }

        Log.debug("Reader thread exited.");
    }

    /**
     * Callback called by the writer thread when it goes away.
     */
    protected synchronized void writerDidExit ()
    {
        // clear out our writer reference
        _writer = null;
        Log.debug("Writer thread exited.");

        // let the client observers know that we're logged off
        _client.notifyObservers(Client.CLIENT_DID_LOGOFF, null);

        // now that the writer thread has gone away, we can safely close
        // our socket and let the client know that the logoff process has
        // completed
        closeChannel();

        // let the client know when we finally go away
        if (_reader == null) {
            _client.communicatorDidExit();
        }
    }

    /**
     * Closes the socket channel that we have open to the server. Called
     * by either {@link #readerDidExit} or {@link #writerDidExit}
     * whichever is called last.
     */
    protected void closeChannel ()
    {
        if (_channel != null) {
            Log.debug("Closing socket channel.");

            try {
                _channel.close();
            } catch (IOException ioe) {
                Log.warning("Error closing failed socket: " + ioe);
            }
            _channel = null;

            // clear these out because they are probably large and in charge
            _oin = null;
            _oout = null;
        }
    }

    /**
     * Writes the supplied message to the socket.
     */
    protected void sendMessage (UpstreamMessage msg)
        throws IOException
    {
        if (_logMessages.getValue()) {
            Log.info("SEND " + msg);
        }

        // first we write the message so that we can measure it's length
        _oout.writeObject(msg);
        _oout.flush();

        // then write the framed message to actual output stream
        try {
            ByteBuffer buffer = _fout.frameAndReturnBuffer();
            int wrote = _channel.write(buffer);
            if (wrote != buffer.limit()) {
                Log.warning("Aiya! Couldn't write entire message [msg=" + msg +
                            ", size=" + buffer.limit() +
                            ", wrote=" + wrote + "].");
//             } else {
//                 Log.info("Wrote " + wrote + " bytes.");
            }

        } finally {
            _fout.resetFrame();
        }

        // make a note of our most recent write time
        updateWriteStamp();
    }

    /**
     * Returns the time at which we last sent a packet to the server.
     */
    protected synchronized long getLastWrite ()
    {
        return _lastWrite;
    }

    /**
     * Makes a note of the time at which we last communicated with the
     * server.
     */
    protected synchronized void updateWriteStamp ()
    {
        _lastWrite = System.currentTimeMillis();
    }

    /**
     * Reads a new message from the socket (blocking until a message has
     * arrived).
     */
    protected DownstreamMessage receiveMessage ()
        throws IOException
    {
        // read in the next message frame (readFrame() can return false
        // meaning it only read part of the frame from the network, in
        // which case we simply call it again because we can't do anything
        // until it has a whole frame; it will throw an exception if it
        // hits EOF or if something goes awry)
        while (!_fin.readFrame(_channel));

        try {
            DownstreamMessage msg = (DownstreamMessage)_oin.readObject();
            if (_logMessages.getValue()) {
                Log.info("RECEIVE " + msg);
            }
            return msg;

        } catch (ClassNotFoundException cnfe) {
            throw new NestableIOException(
                "Unable to decode incoming message.", cnfe);
        }
    }

    /**
     * Callback called by the reader thread when it has parsed a new
     * message from the socket and wishes to have it processed.
     */
    protected void processMessage (DownstreamMessage msg)
    {
        // post this message to the dobjmgr queue
        _omgr.processMessage(msg);
    }

    /**
     * The reader encapsulates the authentication and message reading
     * process. It calls back to the <code>Communicator</code> class to do
     * things, but the general flow of the reader thread is encapsulated
     * in this class.
     */
    protected class Reader extends LoopingThread
    {
        protected void willStart ()
        {
            // first we connect and authenticate with the server
            try {
                // connect to the server
                connect();

                // then authenticate
                logon();

            } catch (Exception e) {
                Log.debug("Logon failed: " + e);
                Log.logStackTrace(e);
                // let the observers know that we've failed
                _client.notifyObservers(Client.CLIENT_FAILED_TO_LOGON, e);
                // and terminate our communicator thread
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
            int port = _client.getPort();

            // establish a socket connection to said server
            Log.debug("Connecting [host=" + host + ", port=" + port + "].");
            _channel = SocketChannel.open(new InetSocketAddress(host, port));
            _channel.configureBlocking(true);

            // our messages are framed (preceded by their length), so we
            // use these helper streams to manage the framing
            _fin = new FramedInputStream();
            _fout = new FramingOutputStream();

            // create our object input and output streams
            _oin = new ObjectInputStream(_fin);
            _oout = new ObjectOutputStream(_fout);
        }

        protected void logon ()
            throws IOException, LogonException
        {
            // construct an auth request and send it
            AuthRequest req = new AuthRequest(_client.getCredentials(),
                                              _client.getVersion());
            sendMessage(req);

            // now wait for the auth response
            Log.debug("Waiting for auth response.");
            AuthResponse rsp = (AuthResponse)receiveMessage();
            AuthResponseData data = rsp.getData();
            Log.debug("Got auth response: " + data);
            
            // if the auth request failed, we want to let the communicator
            // know by throwing a logon exception
            if (!data.code.equals(AuthResponseData.SUCCESS)) {
                throw new LogonException(data.code);
            }

            // we're all clear. let the communicator know that we're in
            logonSucceeded(data);
        }

        // now that we're authenticated, we manage the reading
        // half of things by continuously reading messages from
        // the socket and processing them
        protected void iterate ()
        {
            DownstreamMessage msg = null;

            try {
                // read the next message from the socket
                msg = receiveMessage();

                // process the message
                processMessage(msg);

            } catch (InterruptedIOException iioe) {
                // somebody set up us the bomb! we've been interrupted
                // which means that we're being shut down, so we just
                // report it and return from iterate() like a good monkey
                Log.debug("Reader thread woken up in time to die.");

            } catch (EOFException eofe) {
                // let the communicator know that our connection was
                // closed
                connectionClosed();
                // and shut ourselves down
                shutdown();

            } catch (IOException ioe) {
                // let the communicator know that our connection failed
                connectionFailed(ioe);
                // and shut ourselves down
                shutdown();

            } catch (Exception e) {
                Log.warning("Error processing message [msg=" + msg +
                            ", error=" + e + "].");
            }
        }

        protected void handleIterateFailure (Exception e)
        {
            Log.warning("Uncaught exception it reader thread.");
            Log.logStackTrace(e);
        }

        protected void didShutdown ()
        {
            // let the communicator know when we finally go away
            readerDidExit();
        }

        protected void kick ()
        {
            // we want to interrupt the reader thread as it may be blocked
            // listening to the socket; this is only called if the reader
            // thread doesn't shut itself down
//             interrupt();
        }
    }

    /**
     * The writer encapsulates the message writing process. It calls back
     * to the <code>Communicator</code> class to do things, but the
     * general flow of the writer thread is encapsulated in this class.
     */
    protected class Writer extends LoopingThread
    {
        protected void iterate ()
        {
            // fetch the next message from the queue
            UpstreamMessage msg = (UpstreamMessage)_msgq.get();

            // if this is a termination message, we're being
            // requested to exit, so we want to bail now rather
            // than continuing
            if (msg instanceof TerminationMessage) {
                return;
            }

            try {
                // write the message out the socket
                sendMessage(msg);

            } catch (IOException ioe) {
                // let the communicator know if we have any
                // problems
                connectionFailed(ioe);
                // and bail
                shutdown();
            }
        }

        protected void handleIterateFailure (Exception e)
        {
            Log.warning("Uncaught exception it writer thread.");
            Log.logStackTrace(e);
        }

        protected void didShutdown ()
        {
            writerDidExit();
        }

        protected void kick ()
        {
            // post a bogus message to the outgoing queue to ensure that
            // the writer thread notices that it's time to go
            postMessage(new TerminationMessage());
        }
    }

    /** This is used to terminate the writer thread. */
    protected static class TerminationMessage extends UpstreamMessage
    {
    }

    protected Client _client;
    protected Reader _reader;
    protected Writer _writer;

    protected SocketChannel _channel;
    protected Queue _msgq = new Queue();

    protected long _lastWrite;

    /** We use this to frame our upstream messages. */
    protected FramingOutputStream _fout;
    protected ObjectOutputStream _oout;

    /** We use this to frame our downstream messages. */
    protected FramedInputStream _fin;
    protected ObjectInputStream _oin;

    protected ClientDObjectMgr _omgr;

    /** Used to control low-level message logging. */
    protected static RuntimeAdjust.BooleanAdjust _logMessages =
        new RuntimeAdjust.BooleanAdjust(
            "Toggles whether or not all sent and received low-level " +
            "network events are logged.", "narya.presents.log_events",
            PresentsPrefs.config, false);
}
