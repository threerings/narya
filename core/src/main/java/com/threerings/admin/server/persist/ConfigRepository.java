//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
