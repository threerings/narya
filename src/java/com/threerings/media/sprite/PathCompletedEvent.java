//
// $Id: PathCompletedEvent.java,v 1.2 2002/05/31 03:38:03 mdb Exp $

package com.threerings.media.sprite;

import com.threerings.media.util.Path;

/**
 * A path completed event is dispatched when a sprite completes a path
 * along which it has been requested to move.
 */
public class PathCompletedEvent extends SpriteEvent
{
    /**
     * Constructs a path completed event for the specified sprite and
     * path.
     */
    public PathCompletedEvent (Sprite sprite, Path path)
    {
        super(sprite);
        _path = path;
    }

    /**
     * Returns the path that was just completed.
     */
    public Path getPath ()
    {
        return _path;
    }

    /** A reference to the completed path. */
    protected Path _path;
}
