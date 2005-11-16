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
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;

/**
 * Path related utility functions.
 */
public class PathUtil
{
    /**
     * Computes a rotation to align the X axis with the specified
     * orientation and stores it in the provided target. The provided up
     * vector is crossed with the new orientation to find the left vector
     * and the left vector is recrossed with the orientation to find the
     * correct up vector.
     *
     * @return the supplied target quaternion.
     */
    public static Quaternion computeAxisRotation (
        Vector3f up, Vector3f orient, Quaternion target)
    {
        _axes[0].set(orient);
        _axes[0].normalizeLocal();
        up.cross(_axes[0], _axes[1]);
        _axes[1].normalizeLocal();
        _axes[0].cross(_axes[1], _axes[2]);
        target.fromAxes(_axes);
        return target;
    }

    /**
     * Computes a rotation from the one vector to another.
     *
     * @param axis will be used as the axis of rotation if the two vectors
     * are parallel.
     */
    public static Quaternion computeRotation (
        Vector3f axis, Vector3f from, Vector3f to, Quaternion target)
    {
        float angle = computeAngle(from, to);
        if (angle == FastMath.PI) { // opposite
            target.fromAngleAxis(angle, axis);
        } else if (angle == 0) { // coincident
            target.x = target.y = target.z = 0;
            target.w = 1;
        } else {
            from.cross(to, _axis);
            target.fromAngleAxis(angle, _axis);
        }
        return target;
    }

    /**
     * Computes the angle between two arbitrary vectors.
     */
    public static float computeAngle (Vector3f one, Vector3f two)
    {
        return FastMath.acos(one.dot(two) / (one.length() * two.length()));
    }

    /**
     * Computes the angle between two normalized vectors.
     */
    public static float computeAngleNormal (Vector3f one, Vector3f two)
    {
        return FastMath.acos(one.dot(two));
    }

    protected static Vector3f[] _axes = {
        new Vector3f(), new Vector3f(), new Vector3f() };
    protected static Vector3f _axis = new Vector3f();
}
