//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.presents.peer.net;

import com.samskivert.util.StringUtil;

import com.threerings.util.Name;

import com.threerings.presents.net.UsernamePasswordCreds;

/**
 * Used by peer servers in a cluster installation to authenticate with one
 * another.
 */
public class PeerCreds extends UsernamePasswordCreds
{
    /** A prefix prepended to the node name used as a peer's username to
     * prevent the username from colliding with a normal authenticating user's
     * username. We assume that colons are not allowed in a normal username. */
    public static final String PEER_PREFIX = "peer:";

    /**
     * Creates a unique password for the specified node using the supplied
     * shared secret.
     */
    public static String createPassword (String nodeName, String sharedSecret)
    {
        return StringUtil.md5hex(nodeName + sharedSecret);
    }

    /**
     * Creates credentials for the specified peer.
     */
    public PeerCreds (String nodeName, String sharedSecret)
    {
        super(new Name(PEER_PREFIX + nodeName),
              createPassword(nodeName, sharedSecret));
    }

    /**
     * Used when unserializing an instance from the network.
     */
    public PeerCreds ()
    {
    }

    /**
     * Returns the node name of this authenticating peer (which does not
     * include the {@link #PEER_PREFIX}.
     */
    public String getNodeName ()
    {
        return getUsername().toString().substring(PEER_PREFIX.length());
    }
}
