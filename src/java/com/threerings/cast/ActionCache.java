//
// $Id: ActionCache.java,v 1.1 2002/02/07 03:20:29 mdb Exp $

package com.threerings.cast;

import com.threerings.media.sprite.MultiFrameImage;

/**
 * A mechanism for caching composited character action animations on disk.
 */
public interface ActionCache
{
    /**
     * Fetches from the cache a composited set of images for a particular
     * character for a particular action.
     */
    public MultiFrameImage[] getActionFrames (
        CharacterDescriptor descrip, String action);

    /**
     * Requests that the specified set of action frames for the specified
     * character be cached.
     */
    public void cacheActionFrames (
        CharacterDescriptor descrip, String action, MultiFrameImage[] frames);
}
