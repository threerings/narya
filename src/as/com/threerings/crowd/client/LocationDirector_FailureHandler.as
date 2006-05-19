package com.threerings.crowd.client {

/**
 * Used to recover from a moveTo request that was accepted but
 * resulted in a failed attempt to fetch the place object to which we
 * were moving.
 */
public interface LocationDirector_FailureHandler
{
    /**
     * Should instruct the client to move to the last known working
     * location (as well as clean up after the failed moveTo request).
     */
    function recoverFailedMove (placeId :int) :void;
}

}
