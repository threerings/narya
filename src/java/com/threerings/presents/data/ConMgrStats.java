//
// $Id: ConMgrStats.java,v 1.2 2004/08/04 02:36:56 mdb Exp $

package com.threerings.presents.data;

import com.threerings.io.Streamable;

/**
 * Used to track and report stats on the connection manager.
 */
public class ConMgrStats implements Streamable
{
    /** The current index into the history arrays. */
    public int current;

    /** The size of the queue of waiting to auth sockets. */
    public int[] authQueueSize;

    /** The size of the queue of waiting to die sockets. */
    public int[] deathQueueSize;

    /** The outgoing queue size. */
    public int[] outQueueSize;

    /** The overflow queue size. */
    public int[] overQueueSize;

    /** The number of bytes read. */
    public int[] bytesIn;

    /** The number of bytes written. */
    public int[] bytesOut;

    /** The number of messages read. */
    public int[] msgsIn;

    /** The number of messages written. */
    public int[] msgsOut;

    /** Advances the currently accumulating bucket and clears its
     * previous contents. */
    public void increment ()
    {
        current++;
        authQueueSize[current] = 0;
        deathQueueSize[current] = 0;
        outQueueSize[current] = 0;
        overQueueSize[current] = 0;
        bytesIn[current] = 0;
        bytesOut[current] = 0;
        msgsIn[current] = 0;
        msgsOut[current] = 0;
    }
}
