//
// $Id: SpotMarshaller.java,v 1.3 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.spot.data;

import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.whirled.client.SceneService.SceneMoveListener;
import com.threerings.whirled.data.SceneMarshaller.SceneMoveMarshaller;
import com.threerings.whirled.spot.client.SpotService;
import com.threerings.whirled.spot.client.SpotService.ChangeLocListener;
import com.threerings.whirled.spot.data.Location;

/**
 * Provides the implementation of the {@link SpotService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class SpotMarshaller extends InvocationMarshaller
    implements SpotService
{
    // documentation inherited
    public static class ChangeLocMarshaller extends ListenerMarshaller
        implements ChangeLocListener
    {
        /** The method id used to dispatch {@link #changeLocSucceeded}
         * responses. */
        public static final int CHANGE_LOC_SUCCEEDED = 1;

        // documentation inherited from interface
        public void changeLocSucceeded ()
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, CHANGE_LOC_SUCCEEDED,
                               new Object[] {  }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case CHANGE_LOC_SUCCEEDED:
                ((ChangeLocListener)listener).changeLocSucceeded(
                    );
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    /** The method id used to dispatch {@link #traversePortal} requests. */
    public static final int TRAVERSE_PORTAL = 1;

    // documentation inherited from interface
    public void traversePortal (Client arg1, int arg2, int arg3, SceneMoveListener arg4)
    {
        SceneMoveMarshaller listener4 = new SceneMoveMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, TRAVERSE_PORTAL, new Object[] {
            new Integer(arg2), new Integer(arg3), listener4
        });
    }

    /** The method id used to dispatch {@link #changeLoc} requests. */
    public static final int CHANGE_LOC = 2;

    // documentation inherited from interface
    public void changeLoc (Client arg1, Location arg2, int arg3, ChangeLocListener arg4)
    {
        ChangeLocMarshaller listener4 = new ChangeLocMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, CHANGE_LOC, new Object[] {
            arg2, new Integer(arg3), listener4
        });
    }

    /** The method id used to dispatch {@link #clusterSpeak} requests. */
    public static final int CLUSTER_SPEAK = 3;

    // documentation inherited from interface
    public void clusterSpeak (Client arg1, String arg2, byte arg3)
    {
        sendRequest(arg1, CLUSTER_SPEAK, new Object[] {
            arg2, new Byte(arg3)
        });
    }

    // Generated on 10:09:04 02/05/03.
}
