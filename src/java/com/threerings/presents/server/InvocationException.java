//
// $Id: InvocationException.java,v 1.1 2002/08/14 19:07:56 mdb Exp $

package com.threerings.presents.server;

/**
 * Used to report failures when executing service requests.
 */
public class InvocationException extends Exception
{
    /**
     * Constructs an invocation exception with the supplied cause code
     * string.
     */
    public InvocationException (String cause)
    {
        super(cause);
    }
}
