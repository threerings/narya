//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.web.gwt;

import com.google.common.collect.ComparisonChain;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A GWT-friendly representation of a configuration tuple, consisting of the name of the entry,
 * a type enum, and the toString() of the value.
 */
public class ConfigField
    implements IsSerializable, Comparable<ConfigField>
{
    public enum FieldType
        implements IsSerializable
    {
        INTEGER,
        SHORT,
        BYTE,
        LONG,
        FLOAT,
        BOOLEAN,
        DOUBLE,
        STRING;

        /**
         * Create a string representation of the given value, which should be of the type
         * reflected in this enum.
         */
        public String toString (Object value)
        {
            return (value != null) ? value.toString() : null;
        }

        /**
         * Convert the given string, which should have been created by {@link #toString(Object)},
         * back into its raw value form.
         */
        public Object toValue (String text)
        {
            switch(this) {
            case INTEGER:
                return new Integer(text);
            case SHORT:
                return new Short(text);
            case BYTE:
                return new Byte(text);
            case LONG:
                return new Long(text);
            case FLOAT:
                return new Float(text);
            case DOUBLE:
                return new Double(text);
            case BOOLEAN:
                return new Boolean(text);
            case STRING:
                return text;
            }
            return null;
        }
    }

    public String name;
    public FieldType type;
    public String valStr;

    /** Deserialization constructor. */
    public ConfigField ()
    {
    }

    /** Construct a new ConfigField with the given values. */
    public ConfigField (String name, FieldType type, String valStr)
    {
        this.name = name;
        this.type = type;
        this.valStr = valStr;
    }

    // from Comparable<ConfigField>
    public int compareTo (ConfigField o)
    {
        return ComparisonChain.start().compare(name, o.name).result();
    }

    @Override // from Object
    public boolean equals (Object o)
    {
        return compareTo((ConfigField) o) == 0;
    }

    @Override // from Object
    public int hashCode ()
    {
        return name.hashCode();
    }
}
