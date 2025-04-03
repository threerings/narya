//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.peer.client;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.peer.data.NodeObject.Lock;

/**
 * Defines requests made from one peer to another.
 */
public interface PeerService extends InvocationService<ClientObject>
{
    /**
     * Informs the node that the sending peer ratifies its acquisition or release of the specified
     * lock.
     */
    void ratifyLockAction (Lock lock, boolean acquire);

    /**
     * Requests that the specified action be invoked on this server.
     */
    void invokeAction (byte[] serializedAction);

    /**
     * Requests that the specified request be invoked on this server and wants a confirmation
     *  when it's complete.
     */
    void invokeRequest (byte[] serializedAction, ResultListener listener);

    /**
     * Generates a server status report for this peer and returns it to the supplied listener. The
     * result must be a string.
     *
     * @param type the type of report to generate. See ReportManager for more information.
     */
    void generateReport (String type, ResultListener listener);
}
