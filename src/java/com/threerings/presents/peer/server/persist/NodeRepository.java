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

package com.threerings.presents.peer.server.persist;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import com.samskivert.io.PersistenceException;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.JORARepository;
import com.samskivert.jdbc.jora.Table;

/**
 * Used to share information on active nodes in a Presents server cluster.
 */
public class NodeRepository extends JORARepository
{
    /** The database identifier used when establishing a database connection.
     * This value being <code>nodedb</code>. */
    public static final String NODE_DB_IDENT = "nodedb";

    /**
     * Constructs a new repository with the specified connection provider.
     *
     * @param conprov the connection provider via which we will obtain our
     * database connection.
     */
    public NodeRepository (ConnectionProvider conprov)
        throws PersistenceException
    {
        super(conprov, NODE_DB_IDENT);
    }

    /**
     * Returns a list of all nodes registered in the repository.
     */
    public ArrayList<NodeRecord> loadNodes ()
        throws PersistenceException
    {
        return loadAll(_ntable, "");
    }

    /**
     * Updates the supplied node record, inserting it into the database if
     * necessary.
     */
    public void updateNode (NodeRecord record)
        throws PersistenceException
    {
        store(_ntable, record);
    }

    @Override // documentation inherited
    protected void migrateSchema (Connection conn, DatabaseLiaison liaison)
        throws SQLException, PersistenceException
    {
        JDBCUtil.createTableIfMissing(conn, "NODES", new String[] {
            "NODE_NAME VARCHAR(64) NOT NULL",
            "HOST_NAME VARCHAR(64) NOT NULL",
            "PORT INTEGER NOT NULL",
            "LAST_UPDATED TIMESTAMP NOT NULL",
            "PRIMARY KEY (NODE_NAME)",
        }, "");
    }

    @Override // documentation inherited
    protected void createTables ()
    {
	_ntable = new Table<NodeRecord>(
            NodeRecord.class, "NODES", "NODE_NAME", true);
    }

    protected Table<NodeRecord> _ntable;
}
