//
// $Id: InvocationException.java,v 1.3 2004/08/27 02:20:23 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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
