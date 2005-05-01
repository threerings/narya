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

import com.jme.scene.Controller;

/**
 * Defines a framework for moving sprites around and notifying interested
 * parties when the sprite has completed its path or if the path has been
 * cancelled.
 */
public abstract class Path extends Controller
{
    /**
     * Creates and initializes this path with the sprite it will be
     * manipulating.
     */
    protected Path (Sprite sprite)
    {
        _sprite = sprite;
    }

    /**
     * Called when this path is removed from its sprite (either due to
     * completion or cancellation).
     */
    public void wasRemoved ()
    {
    }

    protected Sprite _sprite;
}
