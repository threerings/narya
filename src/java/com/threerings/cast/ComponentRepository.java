//
// $Id: ComponentRepository.java,v 1.2 2001/10/30 16:16:01 shaper Exp $

package com.threerings.cast;

import java.util.Iterator;

/**
 * The component repository interface is intended to be implemented by
 * classes that provide access to {@link CharacterComponent} objects
 * keyed on their unique component identifier.
 */
public interface ComponentRepository
{
    /**
     * Returns the {@link CharacterComponent} object for the given
     * unique component identifier.
     */
    public CharacterComponent getComponent (int cid)
        throws NoSuchComponentException;

    /**
     * Returns an iterator over the {@link ComponentType} objects
     * representing all available character component types.
     */
    public Iterator enumerateComponentTypes ();

    /**
     * Returns an iterator over the <code>Integer</code> objects
     * representing all available character component identifiers for
     * the given character component type identifier.
     */
    public Iterator enumerateComponentsByType (int ctid);

    /**
     * Returns an iterator over the {@link ComponentClass} objects
     * representing all available character component classes.
     */
    public Iterator enumerateComponentClasses ();

    /**
     * Returns an iterator over the <code>Integer</code> objects
     * representing all available character component identifiers for
     * the given character component type and class identifiers.
     */
    public Iterator enumerateComponentsByClass (int ctid, int clid);
}
