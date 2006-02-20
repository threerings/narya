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

package com.threerings.admin.server.persist;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.JORARepository;
import com.samskivert.jdbc.jora.Session;
import com.samskivert.jdbc.jora.Table;

import com.threerings.admin.Log;

/**
 * Stores configuration information in a database table.
 */
public class ConfigRepository extends JORARepository
{
    /** The identifier used when establishing a database connection. */
    public static final String CONFIG_DB_IDENT = "configdb";

    /**
     * Constructs a new config repository with the specified connection
     * provider.
     */
    public ConfigRepository (ConnectionProvider conprov)
        throws PersistenceException
    {
        super(conprov, CONFIG_DB_IDENT);
    }

    /**
     * Loads up the configuration data for the specified object.
     *
     * @return a map containing field/value pairs for all stored configuration
     * data.
     */
    public HashMap loadConfig (String object)
        throws PersistenceException
    {
        ArrayList list = loadAll(
            _ctable, "where OBJECT = " + JDBCUtil.escape(object));
        HashMap data = new HashMap();
        for (int ii = 0, ll = list.size(); ii < ll; ii++) {
            ConfigDatum datum = (ConfigDatum)list.get(ii);
            data.put(datum.field, datum.value);
        }
        return data;
    }

    /**
     * Updates the specified configuration datum.
     */
    public void updateConfig (String object, String field, String value)
        throws PersistenceException
    {
        ConfigDatum datum = new ConfigDatum();
        datum.object = object;
        datum.field = field;
        datum.value = value;
        store(_ctable, datum);
    }

    // documentation inherited
    protected void migrateSchema (Connection conn, DatabaseLiaison liaison)
        throws SQLException, PersistenceException
    {
        if (!JDBCUtil.tableExists(conn, "CONFIG")) {
            Log.info("Creating admin.config schema...");
            JDBCUtil.loadSchema(conn, "admin/config.sql");
        }
    }

    // documentation inherited
    protected void createTables (Session session)
    {
	_ctable = new Table(
            ConfigDatum.class.getName(), "CONFIG", session, "OBJECT", true);
    }

    protected Table _ctable;
}
