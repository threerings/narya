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
