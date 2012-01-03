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

package com.threerings.crowd.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.threerings.presents.data.ClientObject;
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
        return forClient(_clmgr.getClientObject(visibleName));
    }

    /**
     * Returns the body object to be used for the given client. This is the reverse operation of
     * {@link BodyObject#getClientObject} and the two should match. The default implementation
     * assumes they are one and the same. This method should return null if the client is not
     * currently controlling a body.
     */
    @EventThread
    public BodyObject forClient (ClientObject client)
    {
        return (BodyObject)client;
    }

    @Inject protected ClientManager _clmgr;
}
