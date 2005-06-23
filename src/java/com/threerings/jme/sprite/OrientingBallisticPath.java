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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;

/**
 * A ballistic path that orients the sprite toward the velocity vector as
 * it traverses the path.
 */
public class OrientingBallisticPath extends BallisticPath
{
    /**
     * Creates a {@link BallisticPath} that will rotate the sprite so that
     * the supplied <code>orient</code> is aligned with the velocity
     * vector at all points along the path. If the provided orientation is
     * not initially in line with the starting velocity, the sprite will
     * be rotated immediately and then adjusted as it follows the path.
     */
    public OrientingBallisticPath (
        Sprite sprite, Vector3f orient, Vector3f start, Vector3f velocity,
        Vector3f accel, float duration)
    {
        super(sprite, start, velocity, accel, duration);

        // TODO: handle orient that is not (1, 0, 0)
        _orient = orient;

        // compute the up vector (opposite of acceleration)
        _up = accel.negate();
        _up.normalizeLocal();

        _sprite.setLocalRotation(
            PathUtil.computeAxisRotation(_up, velocity, _rotate));
    }

    // documentation inherited
    public void update (float time)
    {
        // do the normal update
        super.update(time);

        _sprite.setLocalRotation(
            PathUtil.computeAxisRotation(_up, _velocity, _rotate));
    }

    protected Vector3f _orient;
    protected Vector3f _up;
    protected Quaternion _rotate = new Quaternion();
}
