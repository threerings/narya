//
// $Id$

package com.threerings.media.sprite.action;

/**
 * Indicates a Sprite that may or may not be enabled to receive
 * action / hover / arming notifications.
 */
public interface DisableableSprite
{
    /**
     * @return true if this sprite is currently enabled.
     */
    public boolean isEnabled ();
}
