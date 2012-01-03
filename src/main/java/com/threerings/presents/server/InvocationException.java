//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
