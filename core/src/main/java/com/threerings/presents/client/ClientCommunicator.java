//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.io.IOException;

import java.nio.channels.SocketChannel;

import com.samskivert.util.IntListUtil;
import com.samskivert.util.Interval;

import com.samskivert.swing.RuntimeAdjust;

import com.threerings.presents.data.AuthCodes;

import static com.threerings.presents.Log.log;

/**
 * Customizes the blocking communicator with some things that we only do on users' machines (where
 * there's only one client running, not potentially dozens, and where we're not sending high
 * volumes of traffic through the client like we do for inter-server communications). This will go
 * away when we create a special non-blocking communicator for use on the server that integrates
 * with the ClientManager.
 */
public class ClientCommunicator extends BlockingCommunicator
{
    public ClientCommunicator (Client client)
    {
        super(client);
    }

    /**
     * Sets our preferred connection port via our preferences mechanism.
     */
    protected void setPrefPort (String key, int port) {
        PresentsPrefs.config.setValue(key, port);
    }

    /**
     * Gets our preferred connection port via our preferences mechanism.
     */
    protected int getPrefPort (String key, int defaultPort) {
        return PresentsPrefs.config.getValue(key, defaultPort);
    }

    @Override // from BlockingCommunicator
    protected void openChannel (InetAddress host)
        throws IOException
    {
        // obtain the list of available ports on which to attempt our client connection and
        // determine our preferred port
        String pportKey = _client.getHostname() + ".preferred_port";
        int[] ports = _client.getPorts();
        int pport = getPrefPort(pportKey, ports[0]);
        int ppidx = Math.max(0, IntListUtil.indexOf(ports, pport));

        // try connecting on each of the ports in succession
        for (int ii = 0; ii < ports.length; ii++) {
            int port = ports[(ii+ppidx)%ports.length];
            int nextPort = ports[(ii+ppidx+1)%ports.length];
            log.info("Connecting", "host", host, "port", port);
            InetSocketAddress addr = new InetSocketAddress(host, port);
            try {
                synchronized (this) {
                    clearPPI(true);
                    _prefPortInterval = new PrefPortInterval(pportKey, port, nextPort);
                    _channel = SocketChannel.open(addr);
                    _prefPortInterval.schedule(PREF_PORT_DELAY);
                }
                break;

            } catch (IOException ioe) {
                if (ioe instanceof ConnectException && ii < (ports.length-1)) {
                    _client.reportLogonTribulations(
                        new LogonException(AuthCodes.TRYING_NEXT_PORT, true));
                    continue; // try the next port
                }
                throw ioe;
            }
        }
    }

    @Override // from BlockingCommunicator
    protected void readerDidExit ()
    {
        // if we haven't recorded a preferred port yet, instead do the failure action since we
        // didn't stay connected long enough
        clearPPI(true);
        super.readerDidExit();
    }

    @Override // from BlockingCommunicator
    protected boolean debugLogMessages ()
    {
        return _logMessages.getValue();
    }

    /**
     * Cancels our preferred port saving interval. This method is called from the communication
     * reader thread and the interval thread and must thus be synchronized.
     */
    protected synchronized boolean clearPPI (boolean cancel)
    {
        if (_prefPortInterval != null) {
            if (cancel) {
                _prefPortInterval.cancel();
                _prefPortInterval.failed();
            }
            _prefPortInterval = null;
            return true;
        }
        return false;
    }

    /** Used to save our preferred port once we know our connection is not going to be
     * unceremoniously closed by Windows Connection Sharing. */
    protected class PrefPortInterval extends Interval
    {
        public PrefPortInterval (String key, int thisPort, int nextPort) {
            super(Interval.RUN_DIRECT);
            _key = key;
            _thisPort = thisPort;
            _nextPort = nextPort;
        }

        @Override
        public void expired () {
            if (clearPPI(false)) {
                setPrefPort(_key, _thisPort);
            }
        }

        public void failed () {
            setPrefPort(_key, _nextPort);
        }

        protected String _key;
        protected int _thisPort;
        protected int _nextPort;
    }

    /** We use this interval to record the preferred port if it stays connected long enough. */
    protected PrefPortInterval _prefPortInterval;

    /** Used to control low-level message logging. */
    protected static RuntimeAdjust.BooleanAdjust _logMessages =
        new RuntimeAdjust.BooleanAdjust("Toggles whether or not all sent and received low-level " +
                                        "network events are logged.", "narya.presents.log_events",
                                        PresentsPrefs.config, false);

    /** Time a port must remain connected before we mark it as preferred. */
    protected static long PREF_PORT_DELAY = 5000L;
}
