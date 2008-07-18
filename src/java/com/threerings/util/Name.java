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

package com.threerings.util;

import com.threerings.io.SimpleStreamableObject;

/**
 * Contains the name of an entity. Provides a means by which names can be
 * efficiently loosely compared rather than relying on humans to type
 * things exactly or the challenge of inserting code to normalize names
 * everywhere they are compared.
 */
public class Name extends SimpleStreamableObject
    implements Comparable<Name>
{
    /** A blank name for use in situations where it is needed.
     * <em>Note:</em> because names are used in distributed applications
     * you cannot assume that a reference check against blank is
     * sufficient. You must use <code>BLANK.equals(targetName)</code>. */
    public static final Name BLANK = new Name("");

    /**
     * Returns true if this name is null or blank, false if it contains
     * useful data. This works on derived classes as well.
     */
    public static boolean isBlank (Name name)
    {
        return (name == null || name.toString().equals(BLANK.toString()));
    }

    /** Creates a blank instance for unserialization. */
    public Name ()
    {
    }

    /**
     * Creates a name instance with the supplied name.
     */
    public Name (String name)
    {
        _name = (name == null) ? "" : name;
    }

    /**
     * Returns the normalized version of this name.
     */
    public String getNormal ()
    {
        if (_normal == null) {
            _normal = normalize(_name);
            // if _normal is an equals() String, ensure both point to the
            // same string for efficiency
            if (_normal.equals(_name)) {
                _normal = _name;
            }
        }
        return _normal;
    }

    /**
     * Returns true if this name is valid. Derived classes can provide
     * more interesting requirements for name validity than the default
     * which is that it is non-blank.
     */
    public boolean isValid ()
    {
        return !isBlank();
    }

    /**
     * Returns true if this name is blank, false if it contains data.
     */
    public boolean isBlank ()
    {
        return isBlank(this);
    }

    /**
     * Returns the unprocessed name as a string.
     */
    @Override
    public String toString ()
    {
        return _name;
    }

    @Override
    public int hashCode ()
    {
        return getNormal().hashCode();
    }

    @Override
    public boolean equals (Object other)
    {
        if (other != null) {
            Class c = getClass();
            Class oc = other.getClass();
            // we have to be of the same derived class but we don't want to
            // wig out if the classes were loaded from different class loaders
            if (c == oc || c.getName().equals(oc.getName())) {
                return getNormal().equals(((Name)other).getNormal());
            }
        }
        return false;
    }

    // from interface Comparable<Name>
    public int compareTo (Name other)
    {
        return getNormal().compareTo(other.getNormal());
    }

    /**
     * Returns a normalized version of the supplied name. The default
     * implementation is case and space insensitive.
     */
    protected String normalize (String name)
    {
        name = name.toLowerCase();
        // Originally we removed whitespace as part of normalization, but
        // that ran aground when a player was named "Badbob" and an npp
        // was named "Bad Bob". -RG
        //name = _compactor.matcher(name).replaceAll("");
        return name;
    }

    /** The raw name text. */
    protected String _name;

    /** The normalized name text. */
    protected transient String _normal;

    /** Used to strip spaces from names. */
    //protected static Pattern _compactor = Pattern.compile("\\s");
}
