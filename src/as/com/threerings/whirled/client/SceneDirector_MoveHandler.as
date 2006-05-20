package com.threerings.whirled.client {

/**
 * Used to recover from a problem after a completed moveTo.
 */
public interface SceneDirector_MoveHandler
{
    /**
     * Should instruct the client to move the last known working
     * location (as well as clean up after the failed moveTo request).
     */
    function recoverMoveTo (sceneId :int) :void;
}
}
