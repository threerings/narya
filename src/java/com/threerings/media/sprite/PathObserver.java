//
// $Id: PathObserver.java,v 1.2 2003/05/04 18:50:23 mdb Exp $

package com.threerings.media.sprite;

import com.threerings.media.util.Path;

/**
 * An interface to be implemented by classes that would like to be
 * notified when a sprite completes or cancels its path.
 */
public interface PathObserver
{
    /**
     * Called when a sprite's path is cancelled either because a new path
     * was started or the path was explicitly cancelled with {@link
     * Sprite#cancelMove}.
     */
    public void pathCancelled (Sprite sprite, Path path);

    /**
     * Called when a sprite completes its traversal of a path.
     *
     * @param sprite the sprite that completed its path.
     * @param path the path that was completed.
     * @param when the tick stamp of the media tick on which the path was
     * completed (see {@link SpriteManager#tick}) (this may not be in the
     * same time domain as {@link System#currentTimeMillis}).
     */
    public void pathCompleted (Sprite sprite, Path path, long when);
}
