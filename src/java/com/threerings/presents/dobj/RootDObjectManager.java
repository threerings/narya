//
// $Id: RootDObjectManager.java,v 1.1 2001/12/04 01:01:54 mdb Exp $

package com.threerings.presents.dobj;

/**
 * The root distributed object manager extends the basic distributed
 * object manager interface with methods that can only be guaranteed to
 * work in the virtual machine that is hosting the distributed objects in
 * question. VMs that operate proxies of objects can only implement the
 * basic distributed object manager interface.
 */
public interface RootDObjectManager extends DObjectManager
{
    /**
     * Looks up and returns the requested distributed object in the dobj
     * table, returning null if no object exists with that oid.
     */
    public DObject getObject (int oid);
}
