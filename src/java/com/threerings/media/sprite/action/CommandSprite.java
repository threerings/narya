//
// $Id$

package com.threerings.media.sprite.action;

/**
 * Extends CommandSprite to be a sprite that posts CommandEvents to
 * the Controller hierarchy.
 */
public interface CommandSprite extends ActionSprite
{
    /**
     * @return the argument to the action command.
     */
    public Object getCommandArgument();
}
