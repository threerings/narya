//
// $Id: PathObserver.java,v 1.1 2003/04/30 00:44:36 mdb Exp $

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
     */
    public void pathCompleted (Sprite sprite, Path path, long when);
}
