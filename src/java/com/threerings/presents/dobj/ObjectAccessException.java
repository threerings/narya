//
// $Id: ObjectAccessException.java,v 1.5 2003/03/23 01:54:20 mdb Exp $

package com.threerings.presents.dobj;

/**
 * An object access exception is delivered when an object is not
 * accessible to a requesting subscriber for some reason or other. For
 * some access exceptions, special derived classes exist to communicate
 * the error. For others, a message string explaining the access failure
 * is provided.
 */
public class ObjectAccessException extends Exception
{
    /**
     * Constructs a object access exception with the specified error
     * message.
     */
    public ObjectAccessException (String message)
    {
        super(message);
    }

    /**
     * Constructs a object access exception with the specified error
     * message and the chained causing event.
     */
    public ObjectAccessException (String message, Exception cause)
    {
        super(message);
        initCause(cause);
    }

    /**
     * Constructs a object access exception with the specified chained
     * causing event.
     */
    public ObjectAccessException (Exception cause)
    {
        initCause(cause);
    }
}
