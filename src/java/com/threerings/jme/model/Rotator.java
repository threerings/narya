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

package com.threerings.jme.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Properties;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.CloneCreator;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;

import com.samskivert.util.StringUtil;

import com.threerings.jme.Log;

/**
 * A procedural animation that rotates a node around at a constant angular
 * velocity.
 */
public class Rotator extends ModelController
{
    @Override // documentation inherited
    public void configure (Properties props, Spatial target)
    {
        super.configure(props, target);
        String axisstr = props.getProperty("axis", "x"),
            rpsstr = props.getProperty("radpersec", "3.14");
        if (axisstr.equalsIgnoreCase("x")) {
            _axis = Vector3f.UNIT_X;
        } else if (axisstr.equalsIgnoreCase("y")) {
            _axis = Vector3f.UNIT_Y;
        } else if (axisstr.equalsIgnoreCase("z")) {
            _axis = Vector3f.UNIT_Z;
        } else {
            float[] axis = StringUtil.parseFloatArray(axisstr);
            if (axis != null && axis.length == 3) {
                _axis = new Vector3f(axis[0], axis[1],
                    axis[2]).normalizeLocal();
                    
            } else {
                Log.warning("Invalid rotation axis [axis=" + axisstr + "].");
            }
        }
        try {
            _radpersec = Float.parseFloat(rpsstr);
        } catch (NumberFormatException e) {
            Log.warning("Invalid rotation rate [radpersec=" + rpsstr + "].");
        }
    }
    
    // documentation inherited
    public void update (float time)
    {
        if (!isActive()) {
            return;
        }
        _rot.fromAngleNormalAxis(time * _radpersec, _axis);
        _target.getLocalRotation().multLocal(_rot);
    }
    
    @Override // documentation inherited
    public Controller putClone (Controller store, CloneCreator properties)
    {
        Rotator rstore;
        if (store == null) {
            rstore = new Rotator();
        } else {
            rstore = (Rotator)store;
        }
        super.putClone(rstore, properties);
        rstore._axis = _axis;
        rstore._radpersec = _radpersec;
        return rstore;
    }
    
    // documentation inherited from interface Externalizable
    public void writeExternal (ObjectOutput out)
        throws IOException
    {
        super.writeExternal(out);
        out.writeObject(_axis);
        out.writeFloat(_radpersec);
    }
    
    // documentation inherited from interface Externalizable
    public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        super.readExternal(in);
        _axis = (Vector3f)in.readObject();
        _radpersec = in.readFloat();
    }

    /** The axis about which to rotate. */
    protected Vector3f _axis;
    
    /** The velocity at which to rotate in radians per second. */
    protected float _radpersec;
    
    /** A temporary quaternion. */
    protected Quaternion _rot = new Quaternion();
    
    private static final long serialVersionUID = 1;
}
