//
// $Id: LocationMarshaller.java,v 1.2 2002/08/20 19:38:14 mdb Exp $

package com.threerings.crowd.data;

import com.threerings.crowd.client.LocationService;
import com.threerings.crowd.client.LocationService.MoveListener;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link LocationService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 *
 * <p> Generated from <code>
 * $Id: LocationMarshaller.java,v 1.2 2002/08/20 19:38:14 mdb Exp $
 * </code>
 */
public class LocationMarshaller extends InvocationMarshaller
    implements LocationService
{
    // documentation inherited
    public static class MoveMarshaller extends ListenerMarshaller
        implements MoveListener
    {
        /** The method id used to dispatch {@link #moveSucceeded}
         * responses. */
        public static final int MOVE_SUCCEEDED = 1;

        // documentation inherited from interface
        public void moveSucceeded (PlaceConfig arg1)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, MOVE_SUCCEEDED,
                               new Object[] { arg1 }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case MOVE_SUCCEEDED:
                ((MoveListener)listener).moveSucceeded(
                    (PlaceConfig)args[0]);
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    /** The method id used to dispatch {@link #moveTo} requests. */
    public static final int MOVE_TO = 1;

    // documentation inherited from interface
    public void moveTo (Client arg1, int arg2, MoveListener arg3)
    {
        MoveMarshaller listener3 = new MoveMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, MOVE_TO, new Object[] {
            new Integer(arg2), listener3
        });
    }

    // Class file generated on 12:33:02 08/20/02.
}
