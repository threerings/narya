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

package com.threerings.crowd.peer.data;

import com.threerings.util.Name;

import com.threerings.presents.peer.data.ClientInfo;

/**
 * Extends the standard {@link ClientInfo} with Crowd bits.
 */
public class CrowdClientInfo extends ClientInfo
{
    /** The client's visible name, which is used for chatting. */
    public Name visibleName;

    @Override // documentation inherited
    public Comparable getKey ()
    {
        // the PeerManager works in such a way that we can override our client
        // info key and things still work properly; all inter-server
        // communication regarding users will be based on visible name so this
        // makes lookups much more efficient
        return visibleName;
    }
}
