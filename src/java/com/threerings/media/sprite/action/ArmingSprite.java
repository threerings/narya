//
// $Id$

package com.threerings.media.sprite.action;

/**
 * An ActionSprite that wishes to be notified of events when it is armed
 * or not.
 */
public interface ArmingSprite extends ActionSprite
{
    /**
     * Render this sprite such that is is drawn "armed".
     */
    public void setArmed (boolean armed);
}
