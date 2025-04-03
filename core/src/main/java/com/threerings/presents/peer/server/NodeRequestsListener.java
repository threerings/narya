//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.peer.server;

import java.util.Map;

import com.threerings.presents.client.InvocationService;

/**
 * Communicates the {@link NodeRequestsResult} of a {@link PeerManager.NodeRequest} sent to one
 * or more peer nodes.
 */
public interface
    NodeRequestsListener<T> extends InvocationService.InvocationListener
{
    /**
     * Called upon the successful completion of {@link PeerManager#invokeNodeRequest}, regardless
     * of how many nodes were contacted or applicable.
     */
    public void requestsProcessed (NodeRequestsResult<T> result);

    /**
     * Contains the result of a {@link PeerManager.NodeRequest} sent to one or more peer nodes.
     * Any node that returned true for {@link PeerManager.NodeRequest#isApplicable} will appear
     * in either {@link #getNodeResults()} or {@link #getNodeErrors()}. The wasDropped() method
     * will return true iff both mappings are empty.
     */
    public interface NodeRequestsResult<T>
    {
        public Map<String, T> getNodeResults ();
        public Map<String, String> getNodeErrors ();
        boolean wasDropped ();
    }
}
