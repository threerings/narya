//
// $Id$

package com.threerings.media.sprite.action;

/**
 * An Action sprite is a sprite that may be pressed to generate an
 * ActionEvent that will be posted to the Controller hierarchy.
 */
public interface ActionSprite
{
    /**
     * @return the action command to submit if this sprite is clicked.
     */
    public String getActionCommand ();
}
