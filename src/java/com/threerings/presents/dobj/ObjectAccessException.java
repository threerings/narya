//
// $Id: ObjectAccessException.java,v 1.4 2002/04/01 16:49:26 mdb Exp $

package com.threerings.presents.dobj;

import org.apache.commons.lang.exception.NestableException;

/**
 * An object access exception is delivered when an object is not
 * accessible to a requesting subscriber for some reason or other. For
 * some access exceptions, special derived classes exist to communicate
 * the error. For others, a message string explaining the access failure
 * is provided.
 */
public class ObjectAccessException extends NestableException
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
        super(message, cause);
    }

    /**
     * Constructs a object access exception with the specified chained
     * causing event.
     */
    public ObjectAccessException (Exception cause)
    {
        super(cause);
    }
}
