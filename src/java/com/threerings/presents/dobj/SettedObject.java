//
// $Id: SettedObject.java,v 1.1 2004/06/03 16:34:07 ray Exp $

package com.threerings.presents.dobj;

/**
 * Allows manipulation of DSets in generic ways.
 */
public interface SettedObject
{
    /**
     * Get the specified set.
     */
    public DSet getSet (String name);

    /**
     * Add the item to the set.
     */
    public void addToSet (String setName, DSet.Entry entry);

    /**
     * Update an item in the set.
     */
    public void updateSet (String setName, DSet.Entry entry);

    /**
     * Remove the item from set.
     */
    public void removeFromSet (String setName, Comparable key);

    /**
     * See {@link DObject#getOid}.
     */
    public int getOid ();

    /**
     * See {@link DObject#addListener}.
     */
    public void addListener (ChangeListener listener);

    /**
     * See {@link DObject#removeListener}.
     */
    public void removeListener (ChangeListener listener);
}
