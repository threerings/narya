//
// $Id: ComponentRepository.java,v 1.3 2001/11/01 01:40:42 shaper Exp $

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
     * Returns an iterator over the {@link ComponentClass} objects
     * representing all available character component classes.
     */
    public Iterator enumerateComponentClasses ();

    /**
     * Returns an iterator over the <code>Integer</code> objects
     * representing all available character component identifiers for
     * the given character component class identifier.
     */
    public Iterator enumerateComponentsByClass (int clid);
}
