//
// $Id: ComponentRepository.java,v 1.1 2001/10/26 01:17:21 shaper Exp $

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
        throws NoSuchComponentException, NoSuchComponentTypeException;

    /**
     * Returns an iterator over the <code>Integer</code> objects
     * representing all available character component identifiers for
     * the given character component type identifier.
     */
    public Iterator enumerateComponents (int ctid)
        throws NoSuchComponentTypeException;

    /**
     * Returns an iterator over the <code>Integer</code> objects
     * representing all available character component type
     * identifiers.
     */
    public Iterator enumerateComponentTypes ();
}
