//
// $Id: Communicator.java,v 1.1 2001/05/22 06:07:59 mdb Exp $

package com.samskivert.cocktail.cher.client;

import java.io.*;
import java.net.Socket;
import java.net.InetAddress;

import com.samskivert.util.Queue;
import com.samskivert.cocktail.cher.Log;
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
        }

        protected void logon ()
            throws LogonException
        {
        }

        protected void listen ()
        {
        }
    }

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
    protected DataInputStream _dout;
    protected Queue _msgq = new Queue();
}
