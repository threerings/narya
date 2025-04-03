//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
        if (other instanceof Name) {
            Name oname = (Name)other;
            Boolean override = overrideEquals(oname);
            if (override != null || (override = oname.overrideEquals(this)) != null) {
                return override;
            }
            Class<?> c = getClass();
            Class<?> oc = other.getClass();
            // we have to be of the same derived class but we don't want to
            // wig out if the classes were loaded from different class loaders
            if (c == oc || c.getName().equals(oc.getName())) {
                return getNormal().equals(oname.getNormal());
            }
        }
        return false;
    }

    // from interface Comparable<Name>
    public int compareTo (Name other)
    {
        Integer override = overrideCompareTo(other);
        if (override != null) {
            return override;
        } else if ((override = other.overrideCompareTo(this)) != null) {
            return -override;
        }
        Class<?> c = getClass();
        Class<?> oc = other.getClass();
        if (c == oc || c.getName().equals(oc.getName())) {
            return getNormal().compareTo(other.getNormal());
        } else {
            return c.getName().compareTo(oc.getName());
        }
    }

    /**
     * Returns a normalized version of the supplied name. The default implementation is case
     * insensitive.
     */
    protected String normalize (String name)
    {
        return name.toLowerCase();
    }

    /**
     * Gives this name a chance to override the default equality comparison in a symmetric fashion.
     *
     * @return the result of the comparison, or null for no override.
     */
    protected Boolean overrideEquals (Name other)
    {
        return null;
    }

    /**
     * Gives this name a chance to override the default comparison in a symmetric fashion.
     *
     * @return the result of the comparison, or null for no override.
     */
    protected Integer overrideCompareTo (Name other)
    {
        return null;
    }

    /** The raw name text. */
    protected String _name;

    /** The normalized name text. */
    protected transient String _normal;
}
