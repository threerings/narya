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

import com.jme.scene.Node;

import com.jme.math.Quaternion;
import com.jme.math.TransformMatrix;
import com.jme.math.Vector3f;

import com.threerings.jme.Log;

/**
 * Represents a bone in a scene.  Bones can have simple meshes as children,
 * or they can be used to deform {@link SkinMesh}es.
 */
public class BoneNode extends ModelNode
{
    /**
     * No-arg constructor for deserialization.
     */
    public BoneNode ()
    {
    }
    
    /**
     * Default constructor.
     *
     * @param name the name of the node
     */
    public BoneNode (String name)
    {
        super(name);
    }
 
    @Override // documentation inherited
    public void setReferenceTransforms ()
    {
        super.setReferenceTransforms();
        
        // store the inverse of the reference transform
        _invRefTransform.set(worldRotation, worldTranslation);
        _invRefTransform.setScale(worldScale);
        _invRefTransform.inverse();
    }
    
    @Override // documentation inherited
    public void updateWorldVectors ()
    {
        super.updateWorldVectors();
        _skinTransform.set(worldRotation, worldTranslation);
        _skinTransform.setScale(worldScale);
        _skinTransform.multLocal(_invRefTransform, _tempStore);
    }

    /**
     * Returns a reference to the matrix that transforms vertices in the
     * reference position to vertices in the current position.
     */
    public TransformMatrix getSkinTransform ()
    {
        return _skinTransform;
    }
    
    /** The inverse of the bone's world space reference transform. */
    protected TransformMatrix _invRefTransform = new TransformMatrix();
    
    /** The bone's current skin transform. */
    protected TransformMatrix _skinTransform = new TransformMatrix();
    
    /** A temporary vector for transformations. */
    protected Vector3f _tempStore = new Vector3f();
    
    private static final long serialVersionUID = 1;
}
