//
// $Id$

package com.threerings.media.sprite.action;

/**
 * An interface indicating that a sprite wishes to be notified when
 * the mouse hovers over it.
 */
public interface HoverSprite
{
    /**
     * Set the current hover state.
     */
    public void setHovered (boolean hovered);
}
