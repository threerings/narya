package com.threerings.whirled.data {

import com.threerings.io.TypedArray;

import com.threerings.presents.data.ListenerMarshaller;

import com.threerings.crowd.data.PlaceConfig;

import com.threerings.presents.dobj.InvocationResponseEvent;

import com.threerings.whirled.client.SceneService_SceneMoveListener;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

public class SceneMarshaller_SceneMoveMarshaller extends ListenerMarshaller
    implements SceneService_SceneMoveListener
{
    /** The method id used to dispatch {@link #moveSucceeded}
     * responses. */
    public static const MOVE_SUCCEEDED :int = 1;

    // documentation inherited from interface SceneService_SceneMoveListener
    public function moveSucceeded (arg1 :int, arg2 :PlaceConfig) :void
    {
        omgr.postEvent(new InvocationResponseEvent(
                           callerOid, requestId, MOVE_SUCCEEDED,
                           [ arg1, arg2 ]));
    }

    /** The method id used to dispatch {@link #moveSucceededWithScene}
     * responses. */
    public static const MOVE_SUCCEEDED_WITH_SCENE :int = 2;

    // documentation inherited from interface SceneService_SceneMoveListener
    public function moveSucceededWithScene (arg1 :int, arg2 :PlaceConfig, arg3 :SceneModel) :void
    {
        omgr.postEvent(new InvocationResponseEvent(
                           callerOid, requestId, MOVE_SUCCEEDED_WITH_SCENE,
                           [ arg1, arg2, arg3 ]));
    }

    /** The method id used to dispatch {@link #moveSucceededWithUpdates}
     * responses. */
    public static const MOVE_SUCCEEDED_WITH_UPDATES :int = 3;

    // documentation inherited from interface SceneService_SceneMoveListener
    public function moveSucceededWithUpdates (arg1 :int, arg2 :PlaceConfig, arg3 :TypedArray /* of SceneUpdate */) :void
    {
        omgr.postEvent(new InvocationResponseEvent(
                           callerOid, requestId, MOVE_SUCCEEDED_WITH_UPDATES,
                           [ arg1, arg2, arg3 ]));
    }

    // documentation inherited
    public override function dispatchResponse (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case MOVE_SUCCEEDED:
            (listener as SceneService_SceneMoveListener).moveSucceeded(
                args[0] as int, args[1] as PlaceConfig);
            return;

        case MOVE_SUCCEEDED_WITH_SCENE:
            (listener as SceneService_SceneMoveListener).moveSucceededWithScene(
                args[0] as int, args[1] as PlaceConfig, args[2] as SceneModel);
            return;

        case MOVE_SUCCEEDED_WITH_UPDATES:
            (listener as SceneService_SceneMoveListener).moveSucceededWithUpdates(
                args[0] as int, args[1] as PlaceConfig, args[2] as TypedArray);
            return;

        default:
            super.dispatchResponse(methodId, args);
        }
    }
}

}
