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

package com.threerings.jme.camera;

import com.jme.renderer.Camera;

/**
 * Used to move the camera along a particular path.
 */
public abstract class CameraPath
{
    /** Used to inform observers when a camera path is completed or aborted. */
    public interface Observer
    {
        /**
         * Called when this path is finished (potentially early because another
         * path was set before this path completed).
         *
         * @param path the path that was completed.
         *
         * @return true if the observer should remain in the list, false if it
         * should be removed following this notification.
         */
        public boolean pathCompleted (CameraPath path);
    }

    /**
     * This is called on every frame to allow the path to adjust the position
     * of the camera.
     *
     * @return true if the path is completed and can be disposed, false if it
     * is not yet completed.
     */
    public abstract boolean tick (float secondsSince);

    /**
     * Called if this path is aborted prior to completion due to being replaced
     * by a new camera path.
     */
    public void abort ()
    {
    }

    protected CameraPath (CameraHandler camhand)
    {
        _camhand = camhand;
    }

    protected CameraHandler _camhand;
}
