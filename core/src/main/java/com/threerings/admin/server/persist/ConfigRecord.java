//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.server.persist;

import com.samskivert.depot.Key;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.annotation.Column;
import com.samskivert.depot.annotation.Entity;
import com.samskivert.depot.annotation.Id;
import com.samskivert.depot.expression.ColumnExp;

/**
 * Stores information about a configuration entry in the database.
 */
@Entity(name="CONFIG")
public class ConfigRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    public static final Class<ConfigRecord> _R = ConfigRecord.class;
    public static final ColumnExp<String> NODE = colexp(_R, "node");
    public static final ColumnExp<String> OBJECT = colexp(_R, "object");
    public static final ColumnExp<String> FIELD = colexp(_R, "field");
    public static final ColumnExp<String> VALUE = colexp(_R, "value");
    // AUTO-GENERATED: FIELDS END

    public static final int SCHEMA_VERSION = 2;

    @Id
    @Column(name="NODE", length=64)
    public String node;

    @Id
    @Column(name="OBJECT", length=128)
    public String object;

    @Id
    @Column(name="FIELD", length=64)
    public String field;

    @Column(name="VALUE", length=65535)
    public String value;

    /**
     * An empty constructor for unmarshalling.
     */
    public ConfigRecord ()
    {
        super();
    }

    public ConfigRecord (String node, String object, String field, String value)
    {
        super();
        this.node = node;
        this.object = object;
        this.field = field;
        this.value = value;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link ConfigRecord}
     * with the supplied key values.
     */
    public static Key<ConfigRecord> getKey (String node, String object, String field)
    {
        return newKey(_R, node, object, field);
    }

    /** Register the key fields in an order matching the getKey() factory. */
    static { registerKeyFields(NODE, OBJECT, FIELD); }
    // AUTO-GENERATED: METHODS END
}
