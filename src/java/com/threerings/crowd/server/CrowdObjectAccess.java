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

package com.threerings.crowd.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.AccessController;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ProxySubscriber;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.server.PresentsObjectAccess;

import com.threerings.bureau.data.BureauClientObject;
import com.threerings.crowd.data.PlaceObject;

/**
 * Defines the various object access controllers used by the Crowd server.
 */
public class CrowdObjectAccess
{
    /**
     * Provides access control for place objects. The default behavior is to allow place occupants
     * to subscribe to the place object and to use the {@link PresentsObjectAccess#DEFAULT}
     * modification policy.
     */
    public static AccessController PLACE = new AccessController()
    {
        // documentation inherited from interface
        public boolean allowSubscribe (DObject object, Subscriber<?> sub)
        {
            if (sub instanceof ProxySubscriber) {
                ClientObject co = ((ProxySubscriber)sub).getClientObject();
                return ((PlaceObject)object).occupants.contains(co.getOid());
            }
            return true;
        }

        // documentation inherited from interface
        public boolean allowDispatch (DObject object, DEvent event)
        {
            return PresentsObjectAccess.DEFAULT.allowDispatch(object, event);
        }
    };

    /**
     * Extends the access control in {@link #PLACE} to allow Bureau clients to subscribe.
     */
    public static AccessController BUREAU_ACCESS_PLACE = new AccessController() {
        public boolean allowSubscribe (DObject object, Subscriber<?> sub) {
            if (sub instanceof ProxySubscriber) {
                ClientObject co = ((ProxySubscriber)sub).getClientObject();
                if (co instanceof BureauClientObject) {
                    return true;
                }
            }
            return PLACE.allowSubscribe(object, sub);
        }
        public boolean allowDispatch (DObject object, DEvent event) {
            return PLACE.allowDispatch(object, event);
        }
    };

}
