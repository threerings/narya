//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import com.threerings.util.MessageBundle;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.Permission;

/**
 * Used to report failures when executing service requests.
 */
public class InvocationException extends Exception
{
    /**
     * Requires that the specified client have the specified permissions.
     *
     * @throws InvocationException if they do not.
     */
    public static void requireAccess (ClientObject clobj, Permission perm, Object context)
        throws InvocationException
    {
        String errmsg = clobj.checkAccess(perm, context);
        if (errmsg != null) {
            throw new InvocationException(errmsg);
        }
    }

    /**
     * A version of {@link #requireAccess(ClientObject,Permission,Object)} that takes no context.
     */
    public static void requireAccess (ClientObject clobj, Permission perm)
        throws InvocationException
    {
        requireAccess(clobj, perm, null);
    }

    /**
     * Requires that the supplied condition be true, otherwise an invocation exception with the
     * supplied error message is thrown.
     */
    public static void require (boolean condition, String errmsg)
        throws InvocationException
    {
        if (!condition) {
            throw new InvocationException(errmsg);
        }
    }

    /**
     * Constructs an invocation exception with the supplied cause code string.
     */
    public InvocationException (String cause)
    {
        super(cause);
    }

    /**
     * Constructs an invocation exception with the supplied cause code
     * string and qualifying message bundle. The error code will be
     * qualified with the message bundle (see {@link MessageBundle#qualify}).
     */
    public InvocationException (String bundle, String code)
    {
        this(MessageBundle.qualify(bundle, code));
    }
}
