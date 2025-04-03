//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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

import com.google.inject.Inject;
import com.google.inject.Singleton;

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
    @Singleton
    public static class PlaceAccessController implements AccessController
    {
        public boolean allowSubscribe (DObject object, Subscriber<?> sub)
        {
            if (sub instanceof ProxySubscriber) {
                ClientObject co = ((ProxySubscriber)sub).getClientObject();
                return ((PlaceObject)object).occupants.contains(_locator.forClient(co).getOid());
            }
            return true;
        }

        public boolean allowDispatch (DObject object, DEvent event)
        {
            return PresentsObjectAccess.DEFAULT.allowDispatch(object, event);
        }

        @Inject protected BodyLocator _locator;
    };

    /**
     * Extends the access control in {@link PlaceAccessController} to allow Bureau clients to
     * subscribe.
     */
    @Singleton
    public static class BureauAccessController extends PlaceAccessController
    {
        @Override
        public boolean allowSubscribe (DObject object, Subscriber<?> sub) {
            if (sub instanceof ProxySubscriber) {
                ClientObject co = ((ProxySubscriber)sub).getClientObject();
                if (co instanceof BureauClientObject) {
                    return true;
                }
            }
            return super.allowSubscribe(object, sub);
        }
        @Override
        public boolean allowDispatch (DObject object, DEvent event) {
            return super.allowDispatch(object, event);
        }
    };

}
