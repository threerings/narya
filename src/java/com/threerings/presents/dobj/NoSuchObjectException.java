//
// $Id: NoSuchObjectException.java,v 1.2 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.dobj;

/**
 * A no such object exception is delivered when a subscriber requests
 * access to an object that does not exist.
 */
public class NoSuchObjectException extends ObjectAccessException
{
    public NoSuchObjectException (int oid)
    {
        super("m.no_such_object\t" + oid);
    }
}
