//
// $Id: PathAdapter.java,v 1.1 2003/04/30 00:44:36 mdb Exp $

package com.threerings.media.sprite;

import com.threerings.media.util.Path;

/**
 * An adapter class for {@link PathObserver}.
 */
public class PathAdapter implements PathObserver
{
    // documentation inherited from interface
    public void pathCancelled (Sprite sprite, Path path)
    {
    }

    // documentation inherited from interface
    public void pathCompleted (Sprite sprite, Path path, long when)
    {
    }
}
