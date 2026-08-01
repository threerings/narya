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
import com.threerings.presents.dobj.NamedEvent;
import com.threerings.presents.dobj.ProxySubscriber;
import com.threerings.presents.dobj.Subscriber;

import static com.threerings.presents.Log.log;

/**
 * Defines the various object access controllers used by the Presents server.
 */
public enum PresentsObjectAccess implements AccessController
{
    /** Anyone may subscribe, but only the server may dispatch events. */
    DEFAULT {
        // from AccessController
        public boolean allowSubscribe (DObject object, Subscriber<?> subscriber)
        {
            return true;
        }
    },

    /** Provides access control for client objects. */
    CLIENT {
        // from AccessController
        public boolean allowSubscribe (DObject object, Subscriber<?> sub)
        {
            // if the subscriber is a client, ensure that they are this same user
            if (sub instanceof ProxySubscriber proxySub) {
                ClientObject clobj = proxySub.getClientObject();
                if (clobj != object) {
                    log.warning("Refusing ClientObject subscription request",
                                "obj", ((ClientObject)object).who(), "sub", clobj.who());
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean allowDispatch (DObject object, DEvent event)
        {
            if (super.allowDispatch(object, event)) return true;
            // the client is only allowed to modify its own RECEIVERS field
            return event.getSourceOid() == object.getOid() &&
                    event instanceof NamedEvent namedEvent &&
                    ClientObject.RECEIVERS.equals(namedEvent.getName());
        }
    },

    /** Handles the invocation service access. */
    INVOCATION {
        @Override
        public boolean allowDispatch (DObject object, DEvent event)
        {
            return event instanceof InvocationRequestEvent;
        }
    },

    /** Allows nobody to subscribe. */
    PRIVATE,

    ; // end of enum constants
}
