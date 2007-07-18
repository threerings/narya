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

import com.samskivert.jdbc.depot.Key;
import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.annotation.Column;
import com.samskivert.jdbc.depot.annotation.Entity;
import com.samskivert.jdbc.depot.annotation.Id;
import com.samskivert.jdbc.depot.expression.ColumnExp;
import com.samskivert.util.StringUtil;

/**
 * Contains information on an active node in a Presents server cluster.
 */
@Entity
public class NodeRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    /** The column identifier for the {@link #nodeName} field. */
    public static final String NODE_NAME = "nodeName";

    /** The qualified column identifier for the {@link #nodeName} field. */
    public static final ColumnExp NODE_NAME_C =
        new ColumnExp(NodeRecord.class, NODE_NAME);

    /** The column identifier for the {@link #hostName} field. */
    public static final String HOST_NAME = "hostName";

    /** The qualified column identifier for the {@link #hostName} field. */
    public static final ColumnExp HOST_NAME_C =
        new ColumnExp(NodeRecord.class, HOST_NAME);

    /** The column identifier for the {@link #publicHostName} field. */
    public static final String PUBLIC_HOST_NAME = "publicHostName";

    /** The qualified column identifier for the {@link #publicHostName} field. */
    public static final ColumnExp PUBLIC_HOST_NAME_C =
        new ColumnExp(NodeRecord.class, PUBLIC_HOST_NAME);

    /** The column identifier for the {@link #port} field. */
    public static final String PORT = "port";

    /** The qualified column identifier for the {@link #port} field. */
    public static final ColumnExp PORT_C =
        new ColumnExp(NodeRecord.class, PORT);

    /** The column identifier for the {@link #lastUpdated} field. */
    public static final String LAST_UPDATED = "lastUpdated";

    /** The qualified column identifier for the {@link #lastUpdated} field. */
    public static final ColumnExp LAST_UPDATED_C =
        new ColumnExp(NodeRecord.class, LAST_UPDATED);
    // AUTO-GENERATED: FIELDS END

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

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link #NodeRecord}
     * with the supplied key values.
     */
    public static Key<NodeRecord> getKey (String nodeName)
    {
        return new Key<NodeRecord>(
                NodeRecord.class,
                new String[] { NODE_NAME },
                new Comparable[] { nodeName });
    }
    // AUTO-GENERATED: METHODS END
}
