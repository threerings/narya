//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.peer.server.persist;

import java.sql.Timestamp;

import com.samskivert.util.StringUtil;

/**
 * Contains information on an active node in a Presents server cluster.
 */
public class NodeRecord
{
    /** The unique name assigned to this node. */
    public String nodeName;

    /** The DNS name used to connect to this node by other peers. */
    public String hostName;

    /** The DNS name used to connect to this node by normal clients. */
    public String publicHostName;

    /** The port on which to connect to this node. */
    public int port;

    /** The last time this node has reported in. */
    public Timestamp lastUpdated;

    /** Used to create a blank instance when loading from the database. */
    public NodeRecord ()
    {
    }

    /** Creates a record for the specified node. */
    public NodeRecord (String nodeName, String hostName, String publicHostName,
                       int port)
    {
        this.nodeName = nodeName;
        this.hostName = hostName;
        this.publicHostName = publicHostName;
        this.port = port;
    }

    /** Used for queries. */
    public NodeRecord (String nodeName)
    {
        this.nodeName = nodeName;
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
