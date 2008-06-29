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

package com.threerings.crowd.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.threerings.util.Name;

import com.threerings.presents.annotation.EventThread;
import com.threerings.presents.server.ClientManager;

import com.threerings.crowd.data.BodyObject;

/**
 * Used to lookup {@link BodyObject} instances by name.
 */
@Singleton
public class BodyLocator
{
    /**
     * Returns the body object for the user with the specified visible name, or null if they are
     * not online.
     */
    @EventThread
    public BodyObject lookupBody (Name visibleName)
    {
        // by default visibleName is username
        return (BodyObject)_clmgr.getClientObject(visibleName);
    }

    @Inject protected ClientManager _clmgr;
}
