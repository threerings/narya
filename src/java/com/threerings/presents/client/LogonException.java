//
// $Id: LogonException.java,v 1.3 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.client;

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
