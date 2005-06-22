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

import com.jme.math.FastMath;
import com.jme.math.Matrix3f;
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

        // rotate the sprite to start
        rotate(orient, velocity);
    }

    // documentation inherited
    public void update (float time)
    {
        // keep track of the old velocity before we update
        _ovelocity.set(_velocity);

        // do the normal update
        super.update(time);

        // rotate the sprite accordingly
        rotate(_ovelocity, _velocity);
    }

    protected void rotate (Vector3f oorient, Vector3f norient)
    {
        // compute the cross product to get the normal to the plane
        // defined by the velocity vectors
        oorient.cross(norient, _normal);

        // compute the angle between the two vectors
        float angle = FastMath.acos(
            oorient.dot(norient) / (norient.length() * oorient.length()));

        // now use that to compute a rotation matrix from the old to the
        // new vector
        _rotate.fromAngleAxis(angle, _normal);

        // finally rotate the sprite accordingly
        _sprite.getLocalRotation().multLocal(_rotate);
    }

    protected Vector3f _ovelocity = new Vector3f();
    protected Vector3f _normal = new Vector3f();
    protected Quaternion _rotate = new Quaternion();
}
