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
