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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.threerings.jme.Log;

/**
 * A triangle mesh that deforms according to a bone hierarchy.
 */
public class SkinMesh extends ModelMesh
{
    /** Represents the vertex weights of a group of vertices influenced by the
     * same set of bones. */
    public static class WeightGroup
        implements Serializable
    {
        /** The indices of the affected vertices. */
        public int[] indices;
        
        /** The bones influencing this group. */
        public BoneNode[] bones;
        
        /** The array of interleaved weights (of length <code>indices.length *
         * bones.length</code>): weights for first vertex, weights for second,
         * etc. */
        public float[] weights;
        
        private static final long serialVersionUID = 1;
    }
    
    /**
     * No-arg constructor for deserialization.
     */
    public SkinMesh ()
    {
    }
    
    /**
     * Creates an empty mesh.
     */
    public SkinMesh (String name)
    {
        super(name);
    }
    
    /**
     * Creates and populates a mesh.
     */
    public SkinMesh (
        String name, FloatBuffer vertices, FloatBuffer normals,
        FloatBuffer color, FloatBuffer texture, IntBuffer indices)
    {
        super(name, vertices, normals, color, texture, indices);
    }
    
    /**
     * Sets the weight groups that determine how vertices are affected by
     * bones.
     */
    public void setWeightGroups (WeightGroup[] weightGroups)
    {
        _weightGroups = weightGroups;
    }
    
    /**
     * Returns a reference to the array of weight groups.
     */
    public WeightGroup[] getWeightGroups ()
    {
        return _weightGroups;
    }
    
    @Override // documentation inherited
    public void writeExternal (ObjectOutput out)
        throws IOException
    {
        super.writeExternal(out);
        out.writeObject(_weightGroups);
    }
    
    @Override // documentation inherited
    public void readExternal (ObjectInput in)
        throws IOException
    {
        super.readExternal(in);
        try {
            _weightGroups = (WeightGroup[])in.readObject();
        } catch (ClassNotFoundException e) {
            Log.warning("Encounted unknown class [mesh=" + getName() +
                ", exception=" + e + "].");
        }
    }
    
    @Override // documentation inherited
    public void updateWorldData (float time)
    {
        super.updateWorldData(time);
        
        // deform the mesh according to the positions of the bones
        for (int ii = 0; ii < _weightGroups.length; ii++) {
            
        }
    }
    
    /** The groups of vertices influenced by different sets of bones. */
    protected WeightGroup[] _weightGroups;
    
    private static final long serialVersionUID = 1;
}
