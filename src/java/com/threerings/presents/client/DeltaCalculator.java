//
// $Id: DeltaCalculator.java,v 1.3 2003/06/18 17:17:21 mdb Exp $

package com.threerings.presents.client;

import java.util.Arrays;
import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;
import com.threerings.presents.net.PingRequest;
import com.threerings.presents.net.PongResponse;

/**
 * Used to compute the client/server time delta, attempting to account for
 * the network delay experienced when the server sends its current time to
 * the client.
 */
public class DeltaCalculator
{
    /**
     * Constructs a delta calculator which is used to calculate the time
     * delta between the client and server, accounding reasonably well for
     * the delay introduced by sending a timestamp over the network from
     * the server to the client.
     */
    public DeltaCalculator ()
    {
        _deltas = new long[CLOCK_SYNC_PING_COUNT];
    }

    /**
     * Must be called when a ping message is sent to the server.
     */
    public void sentPing (PingRequest ping)
    {
        _ping = ping;
    }

    /**
     * Must be called when the pong response arrives back from the server.
     *
     * @return true if we've iterated sufficiently many times to establish
     * a stable time delta estimate.
     */
    public boolean gotPong (PongResponse pong)
    {
        // make a note of when the ping message was sent and when the pong
        // response was received (both in client time)
        long send = _ping.getPackStamp(), recv = pong.getUnpackStamp();

        // make a note of when the pong response was sent (in server time)
        // and the processing delay incurred on the server
        long server = pong.getPackStamp(), delay = pong.getProcessDelay();

        // compute the network delay (round-trip time divided by two)
        long nettime = (recv - send - delay)/2;

        // the time delta is the client time when the pong was received
        // minus the server's send time (plus network delay): dT = C - S
        _deltas[_iter] = recv - (server + nettime);

        Log.info("Calculated delta [delay=" + delay +
                 ", nettime=" + nettime + ", delta=" + _deltas[_iter] +
                 ", rtt=" + (recv-send) + "].");

        return (++_iter >= CLOCK_SYNC_PING_COUNT);
    }

    /**
     * Returns the best estimate client/server time-delta.
     */
    public long getTimeDelta ()
    {
        // sort the estimates and return one from the middle
        Arrays.sort(_deltas);
        return _deltas[_deltas.length/2];
    }

    /** The number of ping/pong iterations we've made. */
    protected int _iter;

    /** Client/server time delta estimates. */
    protected long[] _deltas;

    /** A reference to the most recently sent ping which we use to obtain
     * the appropriate send stamp when we get the corresponding receive
     * stamp. */
    protected PingRequest _ping;

    /** The number of times we PING during clock sync to try to smooth out
     * network jiggling. */
    protected static final int CLOCK_SYNC_PING_COUNT = 3;
}
