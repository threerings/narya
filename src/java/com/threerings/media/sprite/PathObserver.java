//
// $Id: PathObserver.java,v 1.3 2004/08/27 02:12:41 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
