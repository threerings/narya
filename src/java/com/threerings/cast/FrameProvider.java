//
// $Id: FrameProvider.java,v 1.1 2001/11/27 08:09:35 mdb Exp $

package com.threerings.cast;

import com.threerings.media.sprite.MultiFrameImage;

/**
 * Provides a mechanism where by a character component can obtain access
 * to its image frames for a particular action in an on demand manner.
 */
public interface FrameProvider
{
    /**
     * Returns the animation frames (in the eight sprite directions) for
     * the specified action of the specified component. May return null if
     * the specified action does not exist for the specified component.
     */
    public MultiFrameImage[] getFrames (
        CharacterComponent component, String action);
}
