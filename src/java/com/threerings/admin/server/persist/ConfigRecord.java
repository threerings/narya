//
// $Id: PuzzleManagerDelegate.java 209 2007-02-24 00:37:33Z mdb $
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/vilya/
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

import com.samskivert.jdbc.depot.Key;
import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.annotation.Column;
import com.samskivert.jdbc.depot.annotation.Entity;
import com.samskivert.jdbc.depot.annotation.Id;
import com.samskivert.jdbc.depot.expression.ColumnExp;

@Entity(name="CONFIG")
public class ConfigRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    /** The column identifier for the {@link #node} field. */
    public static final String NODE = "node";

    /** The qualified column identifier for the {@link #node} field. */
    public static final ColumnExp NODE_C =
        new ColumnExp(ConfigRecord.class, NODE);

    /** The column identifier for the {@link #object} field. */
    public static final String OBJECT = "object";

    /** The qualified column identifier for the {@link #object} field. */
    public static final ColumnExp OBJECT_C =
        new ColumnExp(ConfigRecord.class, OBJECT);

    /** The column identifier for the {@link #field} field. */
    public static final String FIELD = "field";

    /** The qualified column identifier for the {@link #field} field. */
    public static final ColumnExp FIELD_C =
        new ColumnExp(ConfigRecord.class, FIELD);

    /** The column identifier for the {@link #value} field. */
    public static final String VALUE = "value";

    /** The qualified column identifier for the {@link #value} field. */
    public static final ColumnExp VALUE_C =
        new ColumnExp(ConfigRecord.class, VALUE);
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

    @Column(name="VALUE", type="TEXT")
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
     * Create and return a primary {@link Key} to identify a {@link #ConfigRecord}
     * with the supplied key values.
     */
    public static Key<ConfigRecord> getKey (String node, String object, String field)
    {
        return new Key<ConfigRecord>(
                ConfigRecord.class,
                new String[] { NODE, OBJECT, FIELD },
                new Comparable[] { node, object, field });
    }
    // AUTO-GENERATED: METHODS END
}
