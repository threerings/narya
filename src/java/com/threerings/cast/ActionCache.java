//
// $Id: ActionCache.java,v 1.2 2002/05/04 19:38:13 mdb Exp $

package com.threerings.cast;

/**
 * A mechanism for caching composited character action animations on disk.
 */
public interface ActionCache
{
    /**
     * Fetches from the cache a composited set of images for a particular
     * character for a particular action.
     */
    public ActionFrames getActionFrames (
        CharacterDescriptor descrip, String action);

    /**
     * Requests that the specified set of action frames for the specified
     * character be cached.
     */
    public void cacheActionFrames (
        CharacterDescriptor descrip, String action, ActionFrames frames);
}
