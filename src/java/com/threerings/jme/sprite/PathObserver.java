//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.jme.sprite;

/**
 * Implement this interface to find out when a sprite completes or cancels
 * its path.
 */
public interface PathObserver extends SpriteObserver
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
     */
    public void pathCompleted (Sprite sprite, Path path);
}
