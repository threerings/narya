//
// $Id: SetAdapter.java,v 1.1 2003/05/20 18:41:46 ray Exp $

package com.threerings.presents.dobj;

/**
 * Implements the methods in SetListener so that you don't have to
 * implement the ones you don't want to.
 */
public class SetAdapter implements SetListener
{
    // documentation inherited from interface SetListener
    public void entryAdded (EntryAddedEvent event)
    {
        // override to provide functionality
    }

    // documentation inherited from interface SetListener
    public void entryUpdated (EntryUpdatedEvent event)
    {
        // override to provide functionality
    }

    // documentation inherited from interface SetListener
    public void entryRemoved (EntryRemovedEvent event)
    {
        // override to provide functionality
    }
}
