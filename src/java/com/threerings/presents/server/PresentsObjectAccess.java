//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.presents.Log;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.AccessController;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.InvocationRequestEvent;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.NamedEvent;
import com.threerings.presents.dobj.Subscriber;

/**
 * Defines the various object access controllers used by the Presents server.
 */
public class PresentsObjectAccess
{
    /**
     * Our default access controller. Disallows modification of any object but allows anyone to
     * subscribe.
     */
    public static AccessController DEFAULT = new AccessController()
    {
        // documentation inherited from interface
        public boolean allowSubscribe (DObject object, Subscriber subscriber)
        {
            // allow anyone to subscribe
            return true;
        }

        // documentation inherited from interface
        public boolean allowDispatch (DObject object, DEvent event)
        {
            // if the event came from the server, it's cool
            if (event.getSourceOid() == -1) {
                return true;

            } else {
                // if it came from the client, it better be a non-modification event
                return (event instanceof MessageEvent || event instanceof InvocationRequestEvent);
            }
        }
    };

    /**
     * Provides access control for client objects.
     */
    public static AccessController CLIENT = new AccessController()
    {
        // documentation inherited from interface
        public boolean allowSubscribe (DObject object, Subscriber sub)
        {
            boolean allowed = true;
            // if the subscriber is a client, ensure that they are this same user
            if (sub instanceof PresentsClient) {
                allowed = ((PresentsClient)sub).getClientObject() == object;
                if (!allowed) {
                    Log.warning("Refusing ClientObject subscription request " +
                                "[obj=" + ((ClientObject)object).who() + ", sub=" + sub + "].");
                }
            }
            return allowed;
        }

        // documentation inherited from interface
        public boolean allowDispatch (DObject object, DEvent event)
        {
            // if the event came from the server, it's cool
            if (event.getSourceOid() == -1) {
                return true;
            }

            // the client is only allowed to modify the RECEIVERS field
            return ((event instanceof NamedEvent) &&
                    ((NamedEvent)event).getName().equals(ClientObject.RECEIVERS));
        }
    };
}
