//
// $Id: SpotMarshaller.java,v 1.4 2003/03/26 02:06:06 mdb Exp $

package com.threerings.whirled.spot.data;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.ConfirmListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.whirled.client.SceneService.SceneMoveListener;
import com.threerings.whirled.data.SceneMarshaller.SceneMoveMarshaller;
import com.threerings.whirled.spot.client.SpotService;
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

    /** The method id used to dispatch {@link #changeLocation} requests. */
    public static final int CHANGE_LOCATION = 2;

    // documentation inherited from interface
    public void changeLocation (Client arg1, Location arg2, ConfirmListener arg3)
    {
        ConfirmMarshaller listener3 = new ConfirmMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, CHANGE_LOCATION, new Object[] {
            arg2, listener3
        });
    }

    /** The method id used to dispatch {@link #joinCluster} requests. */
    public static final int JOIN_CLUSTER = 3;

    // documentation inherited from interface
    public void joinCluster (Client arg1, int arg2, ConfirmListener arg3)
    {
        ConfirmMarshaller listener3 = new ConfirmMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, JOIN_CLUSTER, new Object[] {
            new Integer(arg2), listener3
        });
    }

    /** The method id used to dispatch {@link #clusterSpeak} requests. */
    public static final int CLUSTER_SPEAK = 4;

    // documentation inherited from interface
    public void clusterSpeak (Client arg1, String arg2, byte arg3)
    {
        sendRequest(arg1, CLUSTER_SPEAK, new Object[] {
            arg2, new Byte(arg3)
        });
    }

}
