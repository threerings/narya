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

package com.threerings.presents.peer.net;

import com.threerings.presents.net.ServiceCreds;

/**
 * Used by peer servers in a cluster installation to authenticate with one another.
 */
public class PeerCreds extends ServiceCreds
{
    public PeerCreds (String nodeName, String sharedSecret)
    {
        super(nodeName, sharedSecret);
    }

    /**
     * Used when unserializing an instance from the network.
     */
    public PeerCreds ()
    {
    }
}
