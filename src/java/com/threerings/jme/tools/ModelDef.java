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

package com.threerings.jme.tools;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.jme.scene.Spatial;
import com.jme.util.geom.BufferUtils;

import com.threerings.jme.Log;
import com.threerings.jme.model.BoneNode;
import com.threerings.jme.model.Model;
import com.threerings.jme.model.ModelMesh;
import com.threerings.jme.model.ModelNode;
import com.threerings.jme.model.SkinMesh;

/**
 * An intermediate representation for model nodes using in XML parsing.
 */
public class ModelDef
{
    /** The base class of nodes in the model. */
    public abstract static class SpatialDef
    {
        /** The node's name. */
        public String name;
        
        /** The name of the node's parent. */
        public String parent;
        
        /** The node's transformation. */
        public float[] translation;
        public float[] rotation;
        public float[] scale;
        
        /** Returns a JME node for this definition. */
        public Spatial getSpatial ()
        {
            if (_spatial == null) {
                _spatial = createSpatial();
                setTransform();
            }
            return _spatial;
        }
        
        /** Sets the transform of the created node. */
        protected void setTransform ()
        {
            _spatial.getLocalTranslation().set(translation[0], translation[1],
                translation[2]);
            _spatial.getLocalRotation().set(rotation[0], rotation[1],
                rotation[2], rotation[3]);
            _spatial.getLocalScale().set(scale[0], scale[1], scale[2]);
        }
        
        /** Creates a JME node for this definition. */
        public abstract Spatial createSpatial ();
        
        /** Resolves any name references using the supplied map. */
        public void resolveReferences (HashMap<String, Spatial> nodes)
        {
            Spatial pnode = nodes.get(parent);
            if (pnode instanceof ModelNode) {
                ((ModelNode)pnode).attachChild(_spatial);
            } else if (parent != null) {
                Log.warning("Missing or invalid parent node [spatial=" +
                    name + ", parent=" + parent + "].");
            }
        }
        
        /** The JME node created for this definition. */
        protected Spatial _spatial;
    }
    
    /** A rigid triangle mesh. */
    public static class TriMeshDef extends SpatialDef
    {
        /** The vertices of the mesh. */
        public ArrayList<Vertex> vertices = new ArrayList<Vertex>();
        
        /** The triangle indices. */
        public ArrayList<Integer> indices = new ArrayList<Integer>();
        
        /** Whether or not any of the vertices have texture coordinates. */
        public boolean tcoords;
        
        public void addVertex (Vertex vertex)
        {
            int idx = vertices.indexOf(vertex);
            if (idx != -1) {
                indices.add(idx);
            } else {
                indices.add(vertices.size());
                vertices.add(vertex);
            }
            tcoords = tcoords || vertex.tcoords != null;
        }
        
        // documentation inherited
        public Spatial createSpatial ()
        {
            return setBuffers(new ModelMesh(name));
        }
        
        /** Sets the mesh's geometry buffers and returns it. */
        protected ModelMesh setBuffers (ModelMesh mmesh)
        {
            int vsize = vertices.size();
            ByteBuffer vbbuf = ByteBuffer.allocateDirect(vsize*3*4),
                nbbuf = ByteBuffer.allocateDirect(vsize*3*4),
                tbbuf = tcoords ? ByteBuffer.allocateDirect(vsize*2*4) : null,
                ibbuf = ByteBuffer.allocateDirect(indices.size()*4);
            FloatBuffer vbuf = vbbuf.asFloatBuffer(),
                nbuf = nbbuf.asFloatBuffer(),
                tbuf = tcoords ? tbbuf.asFloatBuffer() : null;
            for (int ii = 0; ii < vsize; ii++) {
                vertices.get(ii).setInBuffers(vbuf, nbuf, tbuf);
            }
            IntBuffer ibuf = ibbuf.asIntBuffer();
            for (int ii = 0, nn = indices.size(); ii < nn; ii++) {
                ibuf.put(indices.get(ii));
            }
            mmesh.reconstruct(vbbuf, nbbuf, null, tbbuf, ibbuf);
            return mmesh;
        }
    }
    
    /** A triangle mesh that deforms according to bone positions. */
    public static class SkinMeshDef extends TriMeshDef
    {
        // documentation inherited
        public Spatial createSpatial ()
        {
            return setBuffers(new SkinMesh(name));
        }
        
        @Override // documentation inherited
        public void resolveReferences (HashMap<String, Spatial> nodes)
        {
            super.resolveReferences(nodes);
            
            // divide the vertices up by weight groups
            HashMap<HashSet<String>, WeightGroupDef> groups =
                new HashMap<HashSet<String>, WeightGroupDef>();
            for (int ii = 0, nn = vertices.size(); ii < nn; ii++) {
                SkinVertex svertex = (SkinVertex)vertices.get(ii);
                HashSet<String> bones = svertex.getBoneNames();
                WeightGroupDef group = groups.get(bones);
                if (group == null) {
                    groups.put(bones, group = new WeightGroupDef());
                }
                group.indices.add(ii);
                for (String bone : bones) {
                    group.weights.add(svertex.getWeight(bone));
                }
            }
            
            // resolve names and set in mesh
            SkinMesh.WeightGroup[] wgroups =
                new SkinMesh.WeightGroup[groups.size()];
            int ii = 0;
            for (Map.Entry<HashSet<String>, WeightGroupDef> entry :
                groups.entrySet()) {
                SkinMesh.WeightGroup wgroup = new SkinMesh.WeightGroup();
                wgroup.indices = toArray(entry.getValue().indices);
                wgroup.bones = new BoneNode[entry.getKey().size()];
                int jj = 0;
                for (String bone : entry.getKey()) {
                    Spatial node = nodes.get(bone);
                    if (!(node instanceof BoneNode)) {
                        Log.warning("Missing or invalid bone reference " +
                            "[mesh=" + name + ", bone=" + bone + "].");
                        return;
                    }
                    wgroup.bones[jj++] = (BoneNode)node;
                }
                wgroup.weights = toArray(entry.getValue().weights);
                wgroups[ii++] = wgroup;
            }
            ((SkinMesh)_spatial).setWeightGroups(wgroups);
        }
    }
    
    /** A generic node. */
    public static class NodeDef extends SpatialDef
    {
        // documentation inherited
        public Spatial createSpatial ()
        {
            return new ModelNode(name);
        }
    }
    
    /** A bone that influences skinned meshes. */
    public static class BoneNodeDef extends NodeDef
    {
        // documentation inherited
        public Spatial createSpatial ()
        {
            return new BoneNode(name);
        }
    }
    
    /** A basic vertex. */
    public static class Vertex
    {
        public float[] location;
        public float[] normal;
        public float[] tcoords;
        
        public void setInBuffers (
            FloatBuffer vbuf, FloatBuffer nbuf, FloatBuffer tbuf)
        {
            vbuf.put(location);
            nbuf.put(normal);
            if (tbuf != null) {
                tbuf.put(tcoords);
            }
        }
        
        public boolean equals (Object obj)
        {
            Vertex overt = (Vertex)obj;
            return Arrays.equals(location, overt.location) &&
                Arrays.equals(normal, overt.normal) &&
                Arrays.equals(tcoords, overt.tcoords);
        }
    }
    
    /** A vertex influenced by a number of bones. */
    public static class SkinVertex extends Vertex
    {
        /** The bones influencing the vertex. */
        public ArrayList<BoneWeight> boneWeights = new ArrayList<BoneWeight>();
        
        public void addBoneWeight (BoneWeight weight)
        {
            boneWeights.add(weight);
        }
        
        /** Returns the set of names of the bones influencing this vertex. */
        public HashSet<String> getBoneNames ()
        {
            HashSet<String> names = new HashSet<String>();
            for (BoneWeight bweight : boneWeights) {
                names.add(bweight.bone);
            }
            return names;
        }
        
        /** Returns the weight of the named bone. */
        public float getWeight (String bone)
        {
            for (BoneWeight bweight : boneWeights) {
                if (bweight.bone.equals(bone)) {
                    return bweight.weight;
                }
            }
            return 0f;
        }
    }
    
    /** The influence of a bone on a vertex. */
    public static class BoneWeight
    {
        /** The name of the influencing bone. */
        public String bone;
        
        /** The amount of influence. */
        public float weight;
    }

    /** A group of vertices influenced by the same bone. */
    public static class WeightGroupDef
    {
        /** The indices of the affected vertex. */
        public ArrayList<Integer> indices = new ArrayList<Integer>();
        
        /** The interleaved vertex weights. */
        public ArrayList<Float> weights = new ArrayList<Float>();
    }
    
    /** The meshes and bones comprising the model. */
    public ArrayList<SpatialDef> spatials = new ArrayList<SpatialDef>();
    
    public void addSpatial (SpatialDef spatial)
    {
        spatials.add(spatial);
    }
    
    /**
     * Creates the model node defined herein.
     */
    public Model createModel (String name)
    {
        Model model = new Model(name);
        
        // start by creating the spatials and mapping them to their names
        HashMap<String, Spatial> nodes = new HashMap<String, Spatial>();
        for (int ii = 0, nn = spatials.size(); ii < nn; ii++) {
            Spatial spatial = spatials.get(ii).getSpatial();
            nodes.put(spatial.getName(), spatial);
        }
        
        // then go through again, resolving any name references and attaching
        // root children
        for (int ii = 0, nn = spatials.size(); ii < nn; ii++) {
            SpatialDef sdef = spatials.get(ii);
            sdef.resolveReferences(nodes);
            if (sdef.getSpatial().getParent() == null) {
                model.attachChild(sdef.getSpatial());
            }
        }
        
        return model;    
    }
    
    /** Converts a boxed Integer list to an unboxed int array. */
    protected static int[] toArray (ArrayList<Integer> list)
    {
        int[] array = new int[list.size()];
        for (int ii = 0, nn = list.size(); ii < nn; ii++) {
            array[ii] = list.get(ii);
        }
        return array;
    }
    
    /** Converts a boxed Float list to an unboxed float array. */
    protected static float[] toArray (ArrayList<Float> list)
    {
        float[] array = new float[list.size()];
        for (int ii = 0, nn = list.size(); ii < nn; ii++) {
            array[ii] = list.get(ii);
        }
        return array;
    }
}
