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

package com.threerings.presents.client {

import flash.utils.getTimer;

import com.threerings.util.Arrays;
import com.threerings.util.Log;

import com.threerings.presents.net.PingRequest;
import com.threerings.presents.net.PongResponse;

/**
 * Used to compute the client/server time delta, attempting to account for
 * the network delay experienced when the server sends its current time to
 * the client.
 */
public class DeltaCalculator
{
    private static const log :Log = Log.getLog(DeltaCalculator);

    /**
     * Constructs a delta calculator which is used to calculate the time
     * delta between the client and server, accounting reasonably well for
     * the delay introduced by sending a timestamp over the network from
     * the server to the client.
     */
    public function DeltaCalculator ()
    {
        _deltas = [];
        for (var ii :int = 0; ii < CLOCK_SYNC_PING_COUNT; ii++) {
            _deltas.push(0);
        }
    }

    /**
     * Should we send another ping?
     */
    public function shouldSendPing () :Boolean
    {
        return (_ping == null) && !isDone();
    }

    /**
     * Must be called when a ping message is sent to the server.
     */
    public function sentPing (ping :PingRequest) :void
    {
        _ping = ping;
    }

    /**
     * Must be called when the pong response arrives back from the server.
     *
     * @return true if we've iterated sufficiently many times to establish
     * a stable time delta estimate.
     */
    public function gotPong (pong :PongResponse) :Boolean
    {
        if (_ping == null) {
            // an errant pong that is likely being processed late after
            // a new connection was opened.
            return false;
        }
        // don't freak out if they keep calling gotPong() after we're done
        if (_iter >= _deltas.length) {
            return true;
        }

        // make a note of when the ping message was sent and when the pong
        // response was received (both in client time)
        var send :Number = _ping.getPackStamp();
        var recv :Number = pong.getUnpackStamp();
        _ping = null; // clear out the saved sent ping

        // Factor to convert a realtime value (such as the server uses) to a number relative to
        //  when flash started up.
        var realtimeToTimer :Number = getTimer() - ((new Date()).getTime());

        // make a note of when the pong response was sent (in server time)
        // and the processing delay incurred on the server
        var server :Number = pong.getPackStamp().toNumber() + realtimeToTimer;
        var delay :Number = pong.getProcessDelay();

        // compute the network delay (round-trip time divided by two)
        var nettime :Number = (recv - send - delay)/2;

        // the time delta is the client time when the pong was received
        // minus the server's send time (plus network delay): dT = C - S
        _deltas[_iter] = recv - (server + nettime);

        log.debug("Calculated delta", "delay", delay, "nettime", nettime, "delta", _deltas[_iter],
                  "rtt", (recv-send));

        return (++_iter >= CLOCK_SYNC_PING_COUNT);
    }

    /**
     * Returns the best estimate client/server time-delta.
     */
    public function getTimeDelta () :Number
    {
        if (_iter == 0) { // no responses yet
            return 0;
        }

        // Return a median value as our estimate, rather than an average.
        // Mdb writes:
        // -----------
        // I used the median because that was more likely to result in a
        // sensible value.
        //
        // Assuming there are two kinds of packets, one that goes and comes
        // back without delay and provides an accurate time value, and one
        // that gets delayed somewhere on the way there or the way back and
        // provides an inaccurate time value.
        //
        // If no packets are delayed, both algorithms should be fine. If one
        // packet is delayed the median will select the middle, non-delayed
        // packet, whereas the average will skew everything a bit because
        // of the delayed packet. If two packets are delayed, the median
        // will be more skewed than the average because it will benefit
        // from the one accurate packet and if all three packets are delayed
        // both algorithms will be (approximately) equally inaccurate.
        //
        // I believe the chances are most likely that zero or one packets
        // will be delayed, so I chose the median rather than the average.
        // -----------

        // copy the deltas array so that we don't alter things before
        // all pongs have arrived
        var deltasCopy :Array = Arrays.copyOf(_deltas);

        // sort the estimates and return one from the middle
        deltasCopy.sort(Array.NUMERIC);
        return deltasCopy[int(deltasCopy.length/2)];
    }

    /**
     * Returns true if this calculator has enough data to compute a time
     * delta estimate. Stick a fork in it!
     */
    public function isDone () :Boolean
    {
        return (_iter >= CLOCK_SYNC_PING_COUNT);
    }

    /** The number of ping/pong iterations we've made. */
    protected var _iter :int;

    /** Client/server time delta estimates. */
    protected var _deltas :Array; // of Number

    /** A reference to the most recently sent ping which we use to obtain
     * the appropriate send stamp when we get the corresponding receive
     * stamp. */
    protected var _ping :PingRequest;

    /** The number of times we PING during clock sync to try to smooth out
     * network jiggling. */
    protected static const CLOCK_SYNC_PING_COUNT :int = 3;
}

}