//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.peer.server.persist;

import java.sql.Timestamp;

import com.samskivert.util.StringUtil;

import com.samskivert.depot.Key;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.annotation.Column;
import com.samskivert.depot.annotation.Entity;
import com.samskivert.depot.annotation.Id;
import com.samskivert.depot.expression.ColumnExp;

/**
 * Contains information on an active node in a Presents server cluster.
 */
@Entity(name="NODES")
public class NodeRecord extends PersistentRecord
{
    /** The types of relationships between nodes. */
    public enum Relationship { PARENT, CHILD, DIRECT_EQUAL, INDIRECT };

    // AUTO-GENERATED: FIELDS START
    public static final Class<NodeRecord> _R = NodeRecord.class;
    public static final ColumnExp<String> NODE_NAME = colexp(_R, "nodeName");
    public static final ColumnExp<String> HOST_NAME = colexp(_R, "hostName");
    public static final ColumnExp<String> PUBLIC_HOST_NAME = colexp(_R, "publicHostName");
    public static final ColumnExp<Integer> PORT = colexp(_R, "port");
    public static final ColumnExp<Timestamp> LAST_UPDATED = colexp(_R, "lastUpdated");
    // AUTO-GENERATED: FIELDS END

    /** Increment this value if you modify the definition of this persistent
     * object in a way that will result in a change to its SQL counterpart. */
    public static final int SCHEMA_VERSION = 1;

    /** The unique name assigned to this node. */
    @Id
    @Column(name="NODE_NAME", length=64)
    public String nodeName;

    /** The DNS name used to connect to this node by other peers. */
    @Column(name="HOST_NAME", length=64)
    public String hostName;

    /** The DNS name used to connect to this node by normal clients. */
    @Column(name="PUBLIC_HOST_NAME", length=64)
    public String publicHostName;

    /** The port on which to connect to this node. */
    @Column(name="PORT")
    public int port;

    /** The last time this node has reported in. */
    @Column(name="LAST_UPDATED")
    public Timestamp lastUpdated;

    /** The relationship of the local node to the node described. */
    public transient Relationship relationship;

    /** Used to create a blank instance when loading from the database. */
    public NodeRecord ()
    {
    }

    /** Creates a record for the specified node. */
    public NodeRecord (String nodeName, String hostName, String publicHostName, int port)
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

    @Override
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link NodeRecord}
     * with the supplied key values.
     */
    public static Key<NodeRecord> getKey (String nodeName)
    {
        return newKey(_R, nodeName);
    }

    /** Register the key fields in an order matching the getKey() factory. */
    static { registerKeyFields(NODE_NAME); }
    // AUTO-GENERATED: METHODS END
}
