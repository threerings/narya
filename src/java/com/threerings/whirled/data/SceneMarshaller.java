//
// $Id: SceneMarshaller.java,v 1.2 2002/08/20 19:38:15 mdb Exp $

package com.threerings.whirled.data;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.whirled.client.SceneService;
import com.threerings.whirled.client.SceneService.SceneMoveListener;
import com.threerings.whirled.data.SceneModel;

/**
 * Provides the implementation of the {@link SceneService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 *
 * <p> Generated from <code>
 * $Id: SceneMarshaller.java,v 1.2 2002/08/20 19:38:15 mdb Exp $
 * </code>
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

        /** The method id used to dispatch {@link #moveSucceededPlusUpdate}
         * responses. */
        public static final int MOVE_SUCCEEDED_PLUS_UPDATE = 2;

        // documentation inherited from interface
        public void moveSucceededPlusUpdate (int arg1, PlaceConfig arg2, SceneModel arg3)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, MOVE_SUCCEEDED_PLUS_UPDATE,
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

            case MOVE_SUCCEEDED_PLUS_UPDATE:
                ((SceneMoveListener)listener).moveSucceededPlusUpdate(
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

    // Class file generated on 12:33:06 08/20/02.
}
