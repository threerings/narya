package com.threerings.whirled.client {

import com.threerings.presents.client.InvocationListener;

import com.threerings.io.TypedArray;

import com.threerings.crowd.data.PlaceConfig;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * Used to communicate the response to a {@link #moveTo} request.
 */
public interface SceneService_SceneMoveListener extends InvocationListener
{
    /**
     * Indicates that a move succeeded.
     *
     * @param placeId the place object id of the newly occupied scene.
     * @param config metadata related to the newly occupied scene.
     */
    function moveSucceeded (placeId :int, config :PlaceConfig) :void;

    /**
     * Indicates that a move succeeded and that the client's cached
     * scene information should be updated with the supplied data.
     *
     * @param placeId the place object id of the newly occupied scene.
     * @param config metadata related to the newly occupied scene.
     * @param updates updates that must be applied to the client's
     * copy of a scene model to bring it up to date.
     */
    function moveSucceededWithUpdates (
            placeId :int, config :PlaceConfig,
            updates :TypedArray /*of SceneUpdate*/) :void;

    /**
     * Indicates that a move succeeded and that the client's cached
     * scene information should be updated with the supplied data.
     *
     * @param placeId the place object id of the newly occupied scene.
     * @param config metadata related to the newly occupied scene.
     * @param model a fresh copy of the most recent scene data for the
     * newly occupied scene.
     */
    function moveSucceededWithScene (
            placeId :int, config :PlaceConfig, model :SceneModel) :void;
}
}
