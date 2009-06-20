//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.data;

import com.threerings.io.Streamable;

/**
 * Encapsulates a fine-grained permissions policy. The default policy is to deny access to
 * everything, systems using fine-grained permissions should create a custom policy and provide it
 * at client resolution time via the ClientResolver.
 */
public class PermissionPolicy
    implements Streamable, InvocationCodes
{
    /**
     * Returns null if the specified client has the specified permission, an error code explaining
     * the lack of access if they do not. {@link InvocationCodes#ACCESS_DENIED} should be returned
     * if no more specific explanation is available.
     */
    public String checkAccess (ClientObject clobj, Permission perm, Object context)
    {
        // by default, you can't do it!
        return ACCESS_DENIED;
    }
}
