//
// $Id: SceneMarshaller.java,v 1.3 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.data;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.whirled.client.SceneService;
import com.threerings.whirled.client.SceneService.SceneMoveListener;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * Provides the implementation of the {@link SceneService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class SceneMarshaller extends InvocationMarshaller
    implements SceneService
{
    // documentation inherited
    public static class SceneMoveMarshaller extends ListenerMarshaller
        implements SceneMoveListener
    {
        /** The method id used to dispatch {@link #moveSucceeded}
         * responses. */
        public static final int MOVE_SUCCEEDED = 1;

        // documentation inherited from interface
        public void moveSucceeded (int arg1, PlaceConfig arg2)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, MOVE_SUCCEEDED,
                               new Object[] { new Integer(arg1), arg2 }));
        }

        /** The method id used to dispatch {@link #moveSucceededWithUpdates}
         * responses. */
        public static final int MOVE_SUCCEEDED_WITH_UPDATES = 2;

        // documentation inherited from interface
        public void moveSucceededWithUpdates (int arg1, PlaceConfig arg2, SceneUpdate[] arg3)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, MOVE_SUCCEEDED_WITH_UPDATES,
                               new Object[] { new Integer(arg1), arg2, arg3 }));
        }

        /** The method id used to dispatch {@link #moveSucceededWithScene}
         * responses. */
        public static final int MOVE_SUCCEEDED_WITH_SCENE = 3;

        // documentation inherited from interface
        public void moveSucceededWithScene (int arg1, PlaceConfig arg2, SceneModel arg3)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, MOVE_SUCCEEDED_WITH_SCENE,
                               new Object[] { new Integer(arg1), arg2, arg3 }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case MOVE_SUCCEEDED:
                ((SceneMoveListener)listener).moveSucceeded(
                    ((Integer)args[0]).intValue(), (PlaceConfig)args[1]);
                return;

            case MOVE_SUCCEEDED_WITH_UPDATES:
                ((SceneMoveListener)listener).moveSucceededWithUpdates(
                    ((Integer)args[0]).intValue(), (PlaceConfig)args[1], (SceneUpdate[])args[2]);
                return;

            case MOVE_SUCCEEDED_WITH_SCENE:
                ((SceneMoveListener)listener).moveSucceededWithScene(
                    ((Integer)args[0]).intValue(), (PlaceConfig)args[1], (SceneModel)args[2]);
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    /** The method id used to dispatch {@link #moveTo} requests. */
    public static final int MOVE_TO = 1;

    // documentation inherited from interface
    public void moveTo (Client arg1, int arg2, int arg3, SceneMoveListener arg4)
    {
        SceneMoveMarshaller listener4 = new SceneMoveMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, MOVE_TO, new Object[] {
            new Integer(arg2), new Integer(arg3), listener4
        });
    }

    // Generated on 14:44:07 02/08/03.
}
