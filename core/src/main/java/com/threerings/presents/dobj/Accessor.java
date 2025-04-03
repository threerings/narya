//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

import java.lang.reflect.Field;

/**
 * Used by {@link DObject} to provide dynamic access to its fields. This class is an implementation
 * detail that can be safely ignored. It is only a public top-level class to ensure that bindings
 * for other languages can make use of it without complications.
 */
public abstract class Accessor implements Comparable<Accessor> {
    /** An accessor that assumes DObject fields are public Java fields. */
    public static class ByField extends Accessor {
        public final Field field;

        public ByField (Field field) {
            super(field.getName());
            this.field = field;
        }

        @Override
        public Object get (DObject obj) {
            try {
                return field.get(obj);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            }
        }

        @Override
        public void set (DObject obj, Object value) {
            try {
                field.set(obj, value);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            }
        }
    }

    /** The name of this attribute. */
    public final String name;

    /** Gets the current value of this attribute. */
    public abstract Object get (DObject obj);

    /** Updates the current value of this attribute. */
    public abstract void set (DObject obj, Object value);

    // from interface Comparable<Accessor>
    public int compareTo (Accessor other)
    {
        return name.compareTo(other.name);
    }

    protected Accessor (String name)
    {
        this.name = name;
    }
}
