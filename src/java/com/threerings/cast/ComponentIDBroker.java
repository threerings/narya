//
// $Id: ComponentIDBroker.java,v 1.1 2001/11/27 08:09:35 mdb Exp $

package com.threerings.cast;

import com.samskivert.io.PersistenceException;

/**
 * Brokers component ids. The component repository interface makes
 * available a collection of components based on a unique identifier. The
 * expectation is that a collection of components will be used to populate
 * a repository and in that population process, component ids will be
 * assigned to the components. The component id broker system provides a
 * means by which named components can be mapped consistently to a set of
 * component ids. Humans can then be responsible for assigning unique
 * names to the components and the broker will ensure that those names map
 * to unique ids that won't change if the repository is rebuilt from the
 * source components.
 */
public interface ComponentIDBroker
{
    /**
     * Returns the unique identifier for the named component. If no
     * identifier has yet been assigned to the specified named component,
     * one should be assigned and returned.
     *
     * @param cclass the name of the class to which the component belongs.
     * @param cname the name of the component.
     *
     * @exception PersistenceException thrown if an error occurs
     * communicating with the underlying persistence mechanism used to
     * store the name to id mappings.
     */
    public int getComponentID (String cclass, String cname)
        throws PersistenceException;

    /**
     * When the user of a component id broker is done obtaining component
     * ids, it must call this method to give the component id broker an
     * opportunity to flush any newly created component ids back to its
     * persistent store.
     */
    public void commit ()
        throws PersistenceException;
}
