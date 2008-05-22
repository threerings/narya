//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2008 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.crowd.data {

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.data.Permission;
import com.threerings.presents.data.PermissionPolicy;

import com.threerings.crowd.chat.data.ChatCodes;

/**
 * Implements some Crowd permissions.
 */
public class CrowdPermissionPolicy extends PermissionPolicy
{
    // from PermissionPolicy
    override public function checkAccess (
        clobj :ClientObject, perm :Permission, context :Object) :String
    {
        if (!(clobj is BodyObject)) {
            return super.checkAccess(clobj, perm, context);
        }

        var body :BodyObject = (clobj as BodyObject);
        if (perm == ChatCodes.BROADCAST_ACCESS) {
            return body.getTokens().isAdmin() ? null : InvocationCodes.ACCESS_DENIED;
        } else if (perm == ChatCodes.CHAT_ACCESS) {
            return null;
        } else {
            return super.checkAccess(clobj, perm, context);
        }
    }
}
}
