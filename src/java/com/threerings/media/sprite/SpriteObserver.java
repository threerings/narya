//
// $Id: SpriteObserver.java,v 1.3 2001/09/13 19:36:20 mdb Exp $

package com.threerings.media.sprite;

/**
 * An interface to be implemented by classes that would like to observe a
 * sprite and be notified of meaningful events as it, and any other
 * sprites, move about.
 */
public interface SpriteObserver
{
    /**
     * This method is called by the {@link SpriteManager} when something
     * interesting is accomplished by or happens to the sprite.
     *
     * @param sprite the sprite involved.
     * @param event the sprite event.
     */
    public void handleEvent (SpriteEvent event);
}
