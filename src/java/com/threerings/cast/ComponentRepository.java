//
// $Id: ComponentRepository.java,v 1.4 2001/11/27 08:09:35 mdb Exp $

package com.threerings.cast;

import java.util.Iterator;

/**
 * Makes available a collection of character components and associated
 * metadata. Character components are animated sequences that can be
 * composited together to create a complete character visualization
 * (imagine interchanging pairs of boots, torsos, hats, etc.).
 */
public interface ComponentRepository
{
    /**
     * Returns the {@link CharacterComponent} object for the given
     * component identifier.
     */
    public CharacterComponent getComponent (int componentId)
        throws NoSuchComponentException;

    /**
     * Iterates over the {@link ComponentClass} instances representing all
     * available character component classes.
     */
    public Iterator enumerateComponentClasses ();

    /**
     * Iterates over the {@link ActionSequence} instances representing
     * every available action sequence.
     */
    public Iterator enumerateActionSequences ();

    /**
     * Iterates over the component ids of all components in the specified
     * class.
     */
    public Iterator enumerateComponentIds (ComponentClass compClass);
}
