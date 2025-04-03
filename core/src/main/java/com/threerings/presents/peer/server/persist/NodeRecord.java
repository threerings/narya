//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.peer.server.persist;

import java.sql.Timestamp;

import com.google.common.base.Objects;

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
    // AUTO-GENERATED: FIELDS START
    public static final Class<NodeRecord> _R = NodeRecord.class;
    public static final ColumnExp<String> NODE_NAME = colexp(_R, "nodeName");
    public static final ColumnExp<String> HOST_NAME = colexp(_R, "hostName");
    public static final ColumnExp<String> PUBLIC_HOST_NAME = colexp(_R, "publicHostName");
    public static final ColumnExp<String> REGION = colexp(_R, "region");
    public static final ColumnExp<Integer> PORT = colexp(_R, "port");
    public static final ColumnExp<Timestamp> LAST_UPDATED = colexp(_R, "lastUpdated");
    public static final ColumnExp<Boolean> SHUTDOWN = colexp(_R, "shutdown");
    // AUTO-GENERATED: FIELDS END

    /** Increment this value if you modify the definition of this persistent
     * object in a way that will result in a change to its SQL counterpart. */
    public static final int SCHEMA_VERSION = 3;

    /** Allow configuration of the use of the public hostname when in different regions. */
    public static final boolean ALWAYS_CONNECT_INTERNAL = Boolean.getBoolean(
            "com.threerings.presents.peer.alwaysConnectInternal");

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

    /** The region in which the node exists.  Nodes in different regions must connect through the
     * public host name. */
    @Column(name="REGION", length=64, nullable=true)
    public String region;

    /** The port on which to connect to this node. */
    @Column(name="PORT")
    public int port;

    /** The last time this node has reported in. */
    @Column(name="LAST_UPDATED")
    public Timestamp lastUpdated;

    /** Whether or not this node is explicitly shut down. */
    @Column(name="SHUTDOWN")
    public boolean shutdown;

    /** Used to create a blank instance when loading from the database. */
    public NodeRecord ()
    {
    }

    /** Creates a record for the specified node. */
    public NodeRecord (
        String nodeName, String hostName, String publicHostName, String region, int port)
    {
        this.nodeName = nodeName;
        this.hostName = hostName;
        this.publicHostName = publicHostName;
        this.region = region;
        this.port = port;
    }

    /** Used for queries. */
    public NodeRecord (String nodeName)
    {
        this.nodeName = nodeName;
    }

    /**
     * Returns the host name to which peers in the specified region should connect.
     */
    public String getPeerHostName (String region)
    {
        return (ALWAYS_CONNECT_INTERNAL || Objects.equal(this.region, region))
                ? hostName
                : publicHostName;
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
