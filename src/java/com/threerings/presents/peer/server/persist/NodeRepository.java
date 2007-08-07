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
import java.util.List;
import java.util.Set;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.depot.DepotRepository;
import com.samskivert.jdbc.depot.PersistenceContext;
import com.samskivert.jdbc.depot.PersistentRecord;

/**
 * Used to share information on active nodes in a Presents server cluster.
 */
public class NodeRepository extends DepotRepository
{
    /**
     * Constructs a new repository with the specified persistence context.
     */
    public NodeRepository (PersistenceContext ctx)
    {
        super(ctx);
    }

    /**
     * Returns a list of all nodes registered in the repository.
     */
    public List<NodeRecord> loadNodes ()
        throws PersistenceException
    {
        return findAll(NodeRecord.class);
    }

    /**
     * Updates the supplied node record, inserting it into the database if necessary.
     */
    public void updateNode (NodeRecord record)
        throws PersistenceException
    {
        record.lastUpdated = new Timestamp(System.currentTimeMillis());
        store(record);
    }

    /**
     * Updates {@link NodeRecord#lastUpdated} for the specified node, indicating that the node is
     * alive and well.
     */
    public void heartbeatNode (String nodeName)
        throws PersistenceException
    {
        updatePartial(NodeRecord.class, nodeName, new Object[] {
            NodeRecord.LAST_UPDATED, new Timestamp(System.currentTimeMillis())
        });
    }

    /**
     * Deletes the identified node record.
     */
    public void deleteNode (String nodeName)
        throws PersistenceException
    {
        // INDEX: Full primary key.
        delete(NodeRecord.class, nodeName);
    }

    @Override // from DepotRepository
    protected void getManagedRecords (Set<Class<? extends PersistentRecord>> classes)
    {
        classes.add(NodeRecord.class);
    }
}
