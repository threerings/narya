//
// $Id: InvocationException.java,v 1.2 2003/03/17 19:21:45 mdb Exp $

package com.threerings.presents.server;

import com.threerings.util.MessageBundle;

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

    /**
     * Constructs an invocation exception with the supplied cause code
     * string and qualifying message bundle. The error code will be
     * qualified with the message bundle (see {@link
     * MessageBundle#qualify}).
     */
    public InvocationException (String bundle, String code)
    {
        this(MessageBundle.qualify(bundle, code));
    }
}
