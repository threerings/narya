//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.AccessController;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.InvocationRequestEvent;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.NamedEvent;
import com.threerings.presents.dobj.ProxySubscriber;
import com.threerings.presents.dobj.Subscriber;

import static com.threerings.presents.Log.log;

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
        public boolean allowSubscribe (DObject object, Subscriber<?> subscriber)
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
        public boolean allowSubscribe (DObject object, Subscriber<?> sub)
        {
            // if the subscriber is a client, ensure that they are this same user
            if (sub instanceof ProxySubscriber) {
                ClientObject clobj = ((ProxySubscriber)sub).getClientObject();
                if (clobj != object) {
                    log.warning("Refusing ClientObject subscription request",
                                "obj", ((ClientObject)object).who(), "sub", clobj.who());
                    return false;
                }
            }
            return true;
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
