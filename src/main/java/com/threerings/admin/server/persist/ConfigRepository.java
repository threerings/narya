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

package com.threerings.admin.server.persist;

import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.Maps;

import com.samskivert.depot.DepotRepository;
import com.samskivert.depot.PersistenceContext;
import com.samskivert.depot.PersistentRecord;

/**
 * Stores configuration information in a database table.
 */
public class ConfigRepository extends DepotRepository
{
    /**
     * Constructs a new config repository with the specified persistence context.
     */
    public ConfigRepository (PersistenceContext ctx)
    {
        super(ctx);
    }

    /**
     * Loads up the configuration data for the specified object.
     *
     * @return a map containing field/value pairs for all stored configuration data.
     */
    public HashMap<String, String> loadConfig (String node, String object)
    {
        HashMap<String, String> data = Maps.newHashMap();
        for (ConfigRecord record : from(ConfigRecord.class).
                 where(ConfigRecord.OBJECT.eq(object), ConfigRecord.NODE.eq(node)).select()) {
            data.put(record.field, record.value);
        }
        return data;
    }

    /**
     * Updates the specified configuration datum.
     */
    public void updateConfig (String node, String object, String field, String value)
    {
        store(new ConfigRecord(node, object, field, value));
    }

    @Override // from DepotRepository
    protected void getManagedRecords (Set<Class<? extends PersistentRecord>> classes)
    {
        classes.add(ConfigRecord.class);
    }
}
