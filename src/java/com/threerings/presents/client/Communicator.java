//
// $Id: Communicator.java,v 1.5 2001/05/30 23:51:39 mdb Exp $

package com.samskivert.cocktail.cher.client;

import java.io.*;
import java.net.Socket;
import java.net.InetAddress;

import com.samskivert.util.LoopingThread;
import com.samskivert.util.Queue;

import com.samskivert.cocktail.cher.Log;
import com.samskivert.cocktail.cher.io.*;
import com.samskivert.cocktail.cher.io.ObjectStreamException;
import com.samskivert.cocktail.cher.net.*;
import com.samskivert.cocktail.cher.util.Codes;

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
        if (_socket == null) {
            return;
        }

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
            // will receive it. we do run the risk that it is in the
            // middle of trying to send a message when we close the
            // socket, but in theory the send will fail, it will complain
            // and then it will cleanly exit. if we were uber paranoid
            // about JVMs misbehaving on simultaneous close()/write(), we
            // could wait here for the writer thread to exit, but that
            // makes me even more nervous because I know that some JVMs
            // don't handle Thread.join() properly (hopefully no one is
            // still using those JVMs but one can never be sure)
            _writer.shutdown();
        }

        // close down our socket
        try {
            _socket.close();
        } catch (IOException cle) {
            Log.warning("Error closing failed socket: " + cle);
        }
        _socket = null;

        // let the client observers know that we're logged off
        _client.notifyObservers(Client.CLIENT_DID_LOGOFF, null);
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
        Log.info("Logon succeeded: " + data);

        // extract bootstrap information

        // create a new writer thread and start it up
        if (_writer != null) {
            throw new RuntimeException("Writer already started!?");
        }
        _writer = new Writer();
        _writer.start();

        // let the client know that logon succeeded
        _client.notifyObservers(Client.CLIENT_DID_LOGON, null);
    }

    /**
     * Callback called by the reader or writer thread when something goes
     * awry with our socket connection to the server.
     */
    protected synchronized void connectionFailed (IOException ioe)
    {
        // make sure the socket isn't already closed down (meaning we've
        // already dealt with the failed connection)
        if (_socket == null) {
            return;
        }

        Log.info("Connection failed: " + ioe);

        // let the client know that things went south
        _client.notifyObservers(Client.CLIENT_CONNECTION_FAILED, ioe);

        // and request that we go through the motions of logging off
        logoff();
    }

    /**
     * Callback called by the reader thread when it goes away.
     */
    protected synchronized void readerDidExit ()
    {
        // clear out our reader reference
        _reader = null;

        // let the client know when we finally go away
        _client.communicatorDidExit();

        Log.info("Reader thread exited.");
    }

    /**
     * Callback called by the writer thread when it goes away.
     */
    protected synchronized void writerDidExit ()
    {
        // clear out our writer reference
        _writer = null;

        Log.info("Writer thread exited.");
    }

    /**
     * Writes the supplied message to the socket.
     */
    protected void sendMessage (UpstreamMessage msg)
        throws IOException
    {
        // first we flatten the message so that we can measure it's length
        TypedObjectFactory.writeTo(_dout, msg);
        // then write the framed message to actual output stream
        _fout.writeFrameAndReset(_out);
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
        while (!_fin.readFrame(_in));

        // then use the typed object factory to read and decode the
        // proper downstream message instance
        return (DownstreamMessage)TypedObjectFactory.readFrom(_din);
    }

    /**
     * Callback called by the reader thread when it has parsed a new
     * message from the socket and wishes to have it processed.
     */
    protected void processMessage (DownstreamMessage msg)
    {
        Log.info("Process msg: " + msg);
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
                Log.info("Logon failed: " + e);
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
            if (_socket != null) {
                throw new IOException("Already connected.");
            }

            // look up the address of the target server
            InetAddress host = InetAddress.getByName(_client.getHostname());

            // establish a socket connection to said server
            _socket = new Socket(host, _client.getPort());

            // get a handle on our input and output streams
            _in = _socket.getInputStream();
            _out = _socket.getOutputStream();

            // our messages are framed (preceded by their length), so we
            // use these helper streams to manage the framing
            _fin = new FramedInputStream();
            _din = new DataInputStream(_fin);
            _fout = new FramingOutputStream();
            _dout = new DataOutputStream(_fout);
        }

        protected void logon ()
            throws IOException, LogonException
        {
            // construct an auth request and send it
            AuthRequest req = new AuthRequest(_client.getCredentials());
            sendMessage(req);

            // now wait for the auth response
            Log.info("Waiting for auth response.");
            AuthResponse rsp = (AuthResponse)receiveMessage();
            AuthResponseData data = rsp.getData();
            Log.info("Got auth response: " + data);
            
            // if the auth request failed, we want to let the communicator
            // know by throwing a login exception
            if (!data.code.equals(Codes.SUCCESS)) {
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

            } catch (ObjectStreamException ose) {
                Log.warning("Error decoding message: " + ose);

            } catch (InterruptedIOException iioe) {
                // somebody set up us the bomb! we've been interrupted
                // which means that we're being shut down, so we just
                // report it and return from iterate() like a good monkey
                Log.info("Reader thread woken up in time to die.");

            } catch (EOFException eofe) {
                Log.info("Connection closed by peer.");
                // nothing left for us to do
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
            interrupt();
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
        public short getType ()
        {
            return -1;
        }
    }

    protected Client _client;
    protected Reader _reader;
    protected Writer _writer;

    protected Socket _socket;
    protected InputStream _in;
    protected OutputStream _out;
    protected Queue _msgq = new Queue();

    /** We use this to frame our upstream messages. */
    protected FramingOutputStream _fout;
    protected DataOutputStream _dout;

    /** We use this to frame our downstream messages. */
    protected FramedInputStream _fin;
    protected DataInputStream _din;
}
