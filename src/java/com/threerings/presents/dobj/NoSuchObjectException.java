//
// $Id: NoSuchObjectException.java,v 1.1 2001/06/01 05:01:52 mdb Exp $

package com.threerings.cocktail.cher.dobj;

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
