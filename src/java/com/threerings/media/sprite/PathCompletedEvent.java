//
// $Id: PathCompletedEvent.java,v 1.3 2002/12/04 02:45:09 shaper Exp $

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
    public PathCompletedEvent (Sprite sprite, long when, Path path)
    {
        super(sprite, when);
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
