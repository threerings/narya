//
// $Id: Communicator.java,v 1.2 2001/05/22 21:51:29 mdb Exp $

package com.samskivert.cocktail.cher.client;

import java.io.*;
import java.net.Socket;
import java.net.InetAddress;

import com.samskivert.util.Queue;

import com.samskivert.cocktail.cher.Log;
import com.samskivert.cocktail.cher.io.FramingOutputStream;
import com.samskivert.cocktail.cher.net.*;

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
class Communicator
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
    public void logoff ()
    {
    }

    /**
     * Queues up the specified message for delivery to the server.
     */
    public void postMessage (UpstreamMessage msg)
    {
        // simply append the message to the queue
        _msgq.append(msg);
    }

    protected void startWriter ()
    {
        if (_writer != null) {
            throw new RuntimeException("Writer already started!?");
        }

        // create a new writer thread and start it up
        _writer = new Writer();
        _writer.start();
    }

    protected synchronized void readerDidExit ()
    {
        // clear out our reader reference
        _reader = null;

        // let the client know when we finally go away
        _client.communicatorDidExit();
    }

    /**
     * Writes the supplied message to the socket.
     */
    protected void sendMessage (UpstreamMessage msg)
        throws IOException
    {
        // first we flatten the message so that we can measure it's length
        msg.writeTo(_dout);
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
        // read the frame size which comes first
        int count = _din.readInt();

        // now read 
        return null;
    }

    /**
     * The reader encapsulates the authentication and message reading
     * process. It calls back to the <code>Communicator</code> class to do
     * things, but the general flow of the reader thread is encapsulated
     * in this class.
     */
    protected class Reader extends Thread
    {
        public void run ()
        {
            try {
                // first we connect and authenticate with the server
                try {
                    // connect to the server
                    connect();

                    // then authenticate
                    logon();

                } catch (Exception e) {
                    // let the observers know that we've failed
                    _client.notifyObservers(Client.CLIENT_FAILED_TO_LOGON, e);
                    // and terminate our communicator thread
                    return;
                }

                // once authenticated, we go into full-duplex mode,
                // starting up another thread to listen for messages while
                // we handle the delivery of messages
                startWriter();
                listen();

            } finally {
                // let the communicator know when we finally go away
                readerDidExit();
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

            // create our input and output streams
            InputStream in = _socket.getInputStream();
            _din = new DataInputStream(new BufferedInputStream(in));
            _out = _socket.getOutputStream();

            // we frame our messages here and then write them directly to
            // the real output stream
            _fout = new FramingOutputStream();
            _dout = new DataOutputStream(_fout);
        }

        protected void logon ()
            throws IOException, LogonException
        {
            // construct an auth request and send it
            AuthRequest req = new AuthRequest(_client.getCredentials());
            sendMessage(req);
        }

        protected void listen ()
        {
        }
    }

    /**
     * The writer encapsulates the message writing process. It calls back
     * to the <code>Communicator</code> class to do things, but the
     * general flow of the writer thread is encapsulated in this class.
     */
    protected class Writer extends Thread
    {
        public void run ()
        {
        }
    }

    protected Client _client;
    protected Reader _reader;
    protected Writer _writer;

    protected Socket _socket;
    protected DataInputStream _din;
    protected OutputStream _out;
    protected Queue _msgq = new Queue();

    /** We use this to frame our upstream messages. */
    protected FramingOutputStream _fout;
    protected DataOutputStream _dout;
}
