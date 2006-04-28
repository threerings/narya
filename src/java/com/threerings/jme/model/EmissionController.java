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

import java.util.Properties;

import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Spatial;
import com.jme.util.geom.BufferUtils;

/**
 * A model controller whose target represents an emitter.
 */
public abstract class EmissionController extends ModelController
{
    @Override // documentation inherited
    public void configure (Properties props, Spatial target)
    {
        // substitute underlying mesh for geometry targets
        if (target instanceof ModelNode) {
            Spatial mesh = ((ModelNode)target).getChild("mesh");
            if (mesh != null) {
                target = mesh;
            }
        }
        super.configure(props, target);
    }
    
    @Override // documentation inherited
    public void init (Model model)
    {
        super.init(model);
        _target.setCullMode(Spatial.CULL_ALWAYS);
    }
    
    /**
     * Determines the current location of the emitter in world coordinates.
     */
    protected void getEmitterLocation (Vector3f result)
    {
        result.set(_target.getWorldTranslation());
    }
    
    /**
     * Determines the current direction of the emitter in world coordinates.
     */
    protected void getEmitterDirection (Vector3f result)
    {
        if (_target instanceof Geometry) {
            BufferUtils.populateFromBuffer(result,
                ((Geometry)_target).getNormalBuffer(), 0);
        } else {
            result.set(0f, 0f, -1f);
        }
        _target.getWorldRotation().multLocal(result);
    }
    
    private static final long serialVersionUID = 1;
}
