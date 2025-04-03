//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.peer.server.persist;

import java.util.List;
import java.util.Set;

import java.sql.Timestamp;

import com.google.common.collect.Lists;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.StringUtil;

import com.samskivert.depot.expression.SQLExpression;
import com.samskivert.depot.DepotRepository;
import com.samskivert.depot.Ops;
import com.samskivert.depot.PersistenceContext;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.Query;

/**
 * Used to share information on active nodes in a Presents server cluster.
 */
@Singleton
public class NodeRepository extends DepotRepository
{
    /**
     * Constructs a new repository with the specified persistence context.
     */
    @Inject public NodeRepository (PersistenceContext ctx)
    {
        super(ctx);
    }

    /**
     * Returns a list of all nodes registered in the repository that are not explicitly shut down.
     */
    public List<NodeRecord> loadNodes ()
    {
        return loadNodes("", false);
    }

    /**
     * Returns a list of all nodes registered in the repository with names starting with the given
     * string that are not explicitly shut down.
     */
    public List<NodeRecord> loadNodes (String namespace)
    {
        return loadNodes(namespace, false);
    }

    /**
     * Returns a list of all nodes registered in the repository with names starting with the given
     * string, optionally including nodes that are explicitly shut down.
     */
    public List<NodeRecord> loadNodes (String namespace, boolean includeShutdown)
    {
        // we specifically avoid caching this query because we want the servers to always see the
        // most up to date set of nodes
        Query<NodeRecord> query = from(NodeRecord.class).noCache();
        List<SQLExpression<?>> conditions = Lists.newArrayList();
        if (!StringUtil.isBlank(namespace)) {
            conditions.add(NodeRecord.NODE_NAME.like(namespace + "%"));
        }
        if (!includeShutdown) {
            conditions.add(Ops.not(NodeRecord.SHUTDOWN));
        }
        return (conditions.isEmpty() ? query : query.where(conditions)).select();
    }

    /**
     * Returns a list of nodes registered in the repository with the specified region that are not
     * explicitly shut down.
     */
    public List<NodeRecord> loadNodesFromRegion (String region)
    {
        return from(NodeRecord.class)
            .noCache()
            .where(NodeRecord.REGION.eq(region), Ops.not(NodeRecord.SHUTDOWN))
            .select();
    }

    /**
     * Updates the supplied node record, inserting it into the database if necessary.
     */
    public void updateNode (NodeRecord record)
    {
        record.lastUpdated = new Timestamp(System.currentTimeMillis());
        store(record);
    }

    /**
     * Updates {@link NodeRecord#lastUpdated} for the specified node, indicating that the node is
     * alive and well.
     */
    public void heartbeatNode (String nodeName)
    {
        updatePartial(NodeRecord.getKey(nodeName),
                      NodeRecord.LAST_UPDATED, new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Marks the identified node as shut down in its record.
     */
    public void shutdownNode (String nodeName)
    {
        updatePartial(NodeRecord.getKey(nodeName), NodeRecord.SHUTDOWN, true);
    }

    @Override // from DepotRepository
    protected void getManagedRecords (Set<Class<? extends PersistentRecord>> classes)
    {
        classes.add(NodeRecord.class);
    }
}
