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
import java.io.Serializable;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import java.util.HashMap;

import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;
import com.jme.renderer.CloneCreator;
import com.jme.renderer.Renderer;
import com.jme.scene.Spatial;
import com.jme.scene.VBOInfo;
import com.jme.util.geom.BufferUtils;

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
        public ModelNode[] bones;
        
        /** The array of interleaved weights (of length <code>indices.length *
         * bones.length</code>): weights for first vertex, weights for second,
         * etc. */
        public float[] weights;
        
        /** The inverses of the bones' mesh space reference transforms. */
        public transient Matrix4f[] invRefTransforms;
        
        /**
         * Rebinds this weight group for a prototype instance.
         *
         * @param pnodes a mapping from prototype nodes to instance nodes
         */
        public WeightGroup rebind (HashMap pnodes)
        {
            WeightGroup group = new WeightGroup();
            group.indices = indices;
            group.weights = weights;
            group.invRefTransforms = invRefTransforms;
            group.bones = new ModelNode[bones.length];
            for (int ii = 0; ii < bones.length; ii++) {
                group.bones[ii] = (ModelNode)pnodes.get(bones[ii]);
            }
            return group;
        }
        
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
    public void reconstruct (
        ByteBuffer vertices, ByteBuffer normals, ByteBuffer colors,
        ByteBuffer textures, ByteBuffer indices)
    {
        super.reconstruct(vertices, normals, colors, textures, indices);
        
        // replace the vertex and normal buffers with working buffers
        setVertexBuffer(BufferUtils.clone(_ovbuf = getVertexBuffer()));
        setNormalBuffer(BufferUtils.clone(_onbuf = getNormalBuffer()));
    }
    
    @Override // documentation inherited
    public Spatial putClone (Spatial store, CloneCreator properties)
    {
        SkinMesh mstore;
        if (store == null) {
            mstore = new SkinMesh(getName());
        } else {
            mstore = (SkinMesh)store;
        }
        properties.removeProperty("vertices");
        properties.removeProperty("normals");
        properties.removeProperty("displaylistid");
        super.putClone(mstore, properties);
        properties.addProperty("vertices");
        properties.addProperty("normals");
        properties.addProperty("displaylistid");
        mstore._weightGroups = new WeightGroup[_weightGroups.length];
        for (int ii = 0; ii < _weightGroups.length; ii++) {
            mstore._weightGroups[ii] =
                _weightGroups[ii].rebind(properties.originalToCopy);
        }
        mstore._ovbuf = _ovbuf;
        mstore._onbuf = _onbuf;
        return mstore;
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
        throws IOException, ClassNotFoundException
    {
        super.readExternal(in);
        _weightGroups = (WeightGroup[])in.readObject();
    }
    
    @Override // documentation inherited
    public void setReferenceTransforms ()
    {
        updateWorldVectors();
        _modelTransform.invert(_transform);
        for (int ii = 0; ii < _weightGroups.length; ii++) {
            WeightGroup group = _weightGroups[ii];
            group.invRefTransforms = new Matrix4f[group.bones.length];
            for (int jj = 0; jj < group.bones.length; jj++) {
                group.invRefTransforms[jj] = _transform.mult(
                    group.bones[jj].getModelTransform()).invertLocal();
            }
        }
    }
    
    @Override // documentation inherited
    public void lockStaticMeshes (
        Renderer renderer, boolean useVBOs, boolean useDisplayLists)
    {
        // we can use VBOs for color, texture, and indices
        if (useVBOs && renderer.supportsVBO()) {
            VBOInfo vboinfo = new VBOInfo(false);
            vboinfo.setVBOColorEnabled(true);
            vboinfo.setVBOTextureEnabled(true);
            vboinfo.setVBOIndexEnabled(true);
            setVBOInfo(vboinfo);
        }
    }
    
    @Override // documentation inherited
    public void updateWorldVectors ()
    {
        super.updateWorldVectors();
        if (parent instanceof ModelNode) {
            ModelNode.setTransform(getLocalTranslation(), getLocalRotation(),
                getLocalScale(), _transform);
            ((ModelNode)parent).getModelTransform().mult(_transform,
                _modelTransform);
            
        } else {
            _modelTransform.loadIdentity();
        }
    }
    
    @Override // documentation inherited
    public void updateWorldData (float time)
    {
        super.updateWorldData(time);
        if (_weightGroups == null) {
            return;
        }
        _modelTransform.invert(_transform);
        
        // deform the mesh according to the positions of the bones
        ModelNode[] bones;
        int idx;
        int[] indices;
        float[] weights;
        Matrix4f xform;
        Matrix4f[] invRefXforms;
        float weight;
        FloatBuffer vbuf = getVertexBuffer(), nbuf = getNormalBuffer();
        for (int ii = 0; ii < _weightGroups.length; ii++) {
            bones = _weightGroups[ii].bones;
            invRefXforms = _weightGroups[ii].invRefTransforms;
            if (_transforms == null || _transforms.length < bones.length) {
                _transforms = new Matrix4f[bones.length];
            }
            for (int jj = 0; jj < bones.length; jj++) {
                if (_transforms[jj] == null) {
                    _transforms[jj] = new Matrix4f();
                }
                _transform.mult(bones[jj].getModelTransform(),
                    _transforms[jj]);
                _transforms[jj].multLocal(invRefXforms[jj]);
            }
            indices = _weightGroups[ii].indices;
            weights = _weightGroups[ii].weights;
            for (int jj = 0, ww = 0; jj < indices.length; jj++) {
                idx = indices[jj];
                BufferUtils.populateFromBuffer(_overtex, _ovbuf, idx);
                BufferUtils.populateFromBuffer(_onormal, _onbuf, idx);
                _vertex.zero();
                _normal.zero();
                for (int kk = 0; kk < bones.length; kk++) {
                    xform = _transforms[kk];
                    weight = weights[ww++];
                    _vertex.addLocal(
                        xform.mult(_overtex, _tmp).multLocal(weight));
                    _normal.addLocal(
                        xform.multAcross(_onormal, _tmp).multLocal(weight));
                }
                BufferUtils.setInBuffer(_vertex, vbuf, idx);
                BufferUtils.setInBuffer(_normal, nbuf, idx);
            }
        }
    }
    
    /** The groups of vertices influenced by different sets of bones. */
    protected WeightGroup[] _weightGroups;
    
    /** The original (undeformed) vertex and normal buffers. */
    protected FloatBuffer _ovbuf, _onbuf;

    /** The node's transform in model space. */
    protected Matrix4f _modelTransform = new Matrix4f();
    
    /** A working array for transforms. */
    protected Matrix4f[] _transforms;

    /** Working vectors. */
    protected Vector3f _overtex = new Vector3f(), _vertex = new Vector3f(),
        _onormal = new Vector3f(), _normal = new Vector3f(),
        _tmp = new Vector3f();
    
    /** Working transform. */
    protected Matrix4f _transform = new Matrix4f();
    
    private static final long serialVersionUID = 1;
}
