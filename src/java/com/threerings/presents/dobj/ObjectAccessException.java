//
// $Id: ObjectAccessException.java,v 1.1 2001/06/01 05:01:52 mdb Exp $

package com.threerings.cocktail.cher.dobj;

/**
 * An object access exception is delivered when an object is not
 * accessible to a requesting subscriber for some reason or other. For
 * some access exceptions, special derived classes exist to communicate
 * the error. For others, a message string explaining the access failure
 * is provided.
 */
public class ObjectAccessException extends Exception
{
    public ObjectAccessException (String message)
    {
        super(message);
    }
}
