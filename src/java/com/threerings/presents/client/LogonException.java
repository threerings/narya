//
// $Id: LogonException.java,v 1.2 2001/05/30 23:58:31 mdb Exp $

package com.threerings.cocktail.cher.client;

/**
 * A logon exception is used to indicate a failure to log on to the
 * server. The reason for failure is encoded as a string and stored in the
 * message field of the exception.
 */
public class LogonException extends Exception
{
    /**
     * Constructs a logon exception with the supplied logon failure code.
     */
    public LogonException (String code)
    {
        super(code);
    }
}
