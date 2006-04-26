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
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import com.jme.math.FastMath;
import com.jme.scene.Spatial;
import com.jme.util.geom.BufferUtils;

import com.samskivert.util.PropertiesUtil;
import com.samskivert.util.StringUtil;

import com.threerings.jme.Log;
import com.threerings.jme.model.Model;
import com.threerings.jme.model.ModelController;
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
        public Spatial getSpatial (Properties props)
        {
            if (_spatial == null) {
                _spatial = createSpatial(new NodeProperties(props, name));
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
        public abstract Spatial createSpatial (Properties props);
        
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
        /** The geometry offset transform. */
        public float[] offsetTranslation;
        public float[] offsetRotation;
        public float[] offsetScale;
        
        /** Whether or not the mesh allows back face culling. */
        public boolean solid;
        
        /** The texture of the mesh, if any. */
        public String texture;
        
        /** Whether or not the mesh is (partially) transparent. */
        public boolean transparent;
        
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
        public Spatial createSpatial (Properties props)
        {
            _mesh = createMesh();
            configureMesh(props);
            ModelNode node = new ModelNode(name);
            node.attachChild(_mesh);
            return node;
        }
        
        /** Creates the mesh to attach to the node. */
        protected ModelMesh createMesh ()
        {
            return new ModelMesh("mesh");
        }
        
        /** Configures the mesh. */
        protected void configureMesh (Properties props)
        {
            // set the geometry offset
            if (offsetTranslation != null) {
                _mesh.getLocalTranslation().set(offsetTranslation[0],
                    offsetTranslation[1], offsetTranslation[2]);
            }
            if (offsetRotation != null) {
                _mesh.getLocalRotation().set(offsetRotation[0],
                    offsetRotation[1], offsetRotation[2], offsetRotation[3]);
            }
            if (offsetScale != null) {
                _mesh.getLocalScale().set(offsetScale[0], offsetScale[1],
                    offsetScale[2]);
            }
            
            // make sure texture is just a filename
            int sidx = (texture == null) ? -1 :
                Math.max(texture.lastIndexOf('/'), texture.lastIndexOf('\\'));
            if (sidx != -1) {
                texture = texture.substring(sidx + 1);
            }
            
            // configure using properties
            _mesh.configure(solid, texture, transparent, props);
            
            // set the various buffers
            int vsize = vertices.size();
            ByteOrder no = ByteOrder.nativeOrder();
            ByteBuffer vbbuf = ByteBuffer.allocateDirect(vsize*3*4).order(no),
                nbbuf = ByteBuffer.allocateDirect(vsize*3*4).order(no),
                tbbuf = tcoords ?
                    ByteBuffer.allocateDirect(vsize*2*4).order(no) : null,
                ibbuf = ByteBuffer.allocateDirect(indices.size()*4).order(no);
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
            _mesh.reconstruct(vbbuf, nbbuf, null, tbbuf, ibbuf);
            
            // set the mesh's origin to the center of its bounding box
            _mesh.centerVertices();
        }
        
        /** The mesh that contains the actual geometry. */
        protected ModelMesh _mesh;
    }
    
    /** A triangle mesh that deforms according to bone positions. */
    public static class SkinMeshDef extends TriMeshDef
    {
        @Override // documentation inherited
        protected ModelMesh createMesh ()
        {
            return new SkinMesh("mesh");
        }
        
        @Override // documentation inherited
        public void resolveReferences (HashMap<String, Spatial> nodes)
        {
            super.resolveReferences(nodes);
            
            // divide the vertices up by weight groups
            HashMap<HashSet<ModelNode>, WeightGroupDef> groups =
                new HashMap<HashSet<ModelNode>, WeightGroupDef>();
            for (int ii = 0, nn = vertices.size(); ii < nn; ii++) {
                SkinVertex svertex = (SkinVertex)vertices.get(ii);
                HashSet<ModelNode> bones = svertex.getBones(nodes);
                WeightGroupDef group = groups.get(bones);
                if (group == null) {
                    groups.put(bones, group = new WeightGroupDef());
                }
                group.indices.add(ii);
                for (ModelNode bone : bones) {
                    group.weights.add(svertex.getWeight(bone));
                }
            }
            
            // resolve names and set in mesh
            SkinMesh.WeightGroup[] wgroups =
                new SkinMesh.WeightGroup[groups.size()];
            int ii = 0;
            for (Map.Entry<HashSet<ModelNode>, WeightGroupDef> entry :
                groups.entrySet()) {
                SkinMesh.WeightGroup wgroup = new SkinMesh.WeightGroup();
                wgroup.indices = toArray(entry.getValue().indices);
                HashSet<ModelNode> bones = entry.getKey();
                wgroup.bones = bones.toArray(new ModelNode[bones.size()]);
                wgroup.weights = toArray(entry.getValue().weights);
                wgroups[ii++] = wgroup;
            }
            ((SkinMesh)_mesh).setWeightGroups(wgroups);
        }
    }
    
    /** A generic node. */
    public static class NodeDef extends SpatialDef
    {
        // documentation inherited
        public Spatial createSpatial (Properties props)
        {
            return new ModelNode(name);
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
            
            // make sure the normal is normalized
            float nlen = FastMath.sqrt(normal[0]*normal[0] +
                normal[1]*normal[1] + normal[2]*normal[2]);
            if (nlen != 1f) {
                normal[0] /= nlen;
                normal[1] /= nlen;
                normal[2] /= nlen;
            }
            nbuf.put(normal);
            
            if (tbuf != null) {
                if (tcoords != null) {
                    tbuf.put(tcoords);
                } else {
                    tbuf.put(0f);
                    tbuf.put(0f);
                }
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
        /** The bones influencing the vertex, mapped by name. */
        public HashMap<String, BoneWeight> boneWeights =
            new HashMap<String, BoneWeight>();
        
        public void addBoneWeight (BoneWeight weight)
        {
            if (weight.weight == 0f) {
                return;
            }
            BoneWeight bweight = boneWeights.get(weight.bone);
            if (bweight != null) {
                bweight.weight += weight.weight;
            } else {
                boneWeights.put(weight.bone, weight);
            }
        }
        
        /** Finds the bone nodes influencing this vertex. */
        public HashSet<ModelNode> getBones (HashMap<String, Spatial> nodes)
        {
            HashSet<ModelNode> bones = new HashSet<ModelNode>();
            for (String bone : boneWeights.keySet()) {
                Spatial node = nodes.get(bone);
                if (node instanceof ModelNode) {
                    bones.add((ModelNode)node);
                } else {
                    Log.warning("Missing or invalid bone for bone weight " +
                        "[bone=" + bone + "].");
                }
            }
            return bones;
        }
        
        /** Returns the weight of the given bone. */
        public float getWeight (ModelNode bone)
        {
            BoneWeight bweight = boneWeights.get(bone.getName());
            return (bweight == null) ? 0f : bweight.weight;
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
        // put nodes before meshes so that bones are updated before skin
        spatials.add(spatial instanceof NodeDef ?  0 : spatials.size(),
            spatial);
    }
    
    /**
     * Creates the model node defined herein.
     *
     * @param props the properties of the model
     * @param nodes a node map to populate
     */
    public Model createModel (Properties props, HashMap<String, Spatial> nodes)
    {
        Model model = new Model(props.getProperty("name", "model"), props);
        
        // set the overall scale
        model.setLocalScale(Float.parseFloat(props.getProperty("scale", "1")));
        
        // start by creating the spatials and mapping them to their names
        for (int ii = 0, nn = spatials.size(); ii < nn; ii++) {
            Spatial spatial = spatials.get(ii).getSpatial(props);
            nodes.put(spatial.getName(), spatial);
        }
        
        // then go through again, resolving any name references and attaching
        // root children
        for (int ii = 0, nn = spatials.size(); ii < nn; ii++) {
            SpatialDef sdef = spatials.get(ii);
            sdef.resolveReferences(nodes);
            if (sdef.getSpatial(props).getParent() == null) {
                model.attachChild(sdef.getSpatial(props));
            }
        }
        
        // create any controllers listed
        String[] controllers = StringUtil.parseStringArray(
            props.getProperty("controllers", ""));
        for (int ii = 0; ii < controllers.length; ii++) {
            Spatial target = nodes.get(controllers[ii]);
            if (target == null) {
                Log.warning("Missing controller node [name=" +
                    controllers[ii] + "].");
                continue;
            }
            ModelController ctrl = createController(
                PropertiesUtil.getSubProperties(props, controllers[ii]),
                target);
            if (ctrl != null) {
                model.addController(ctrl);
            }
        }
        
        return model;
    }
    
    /** Creates, configures, and returns a model controller. */
    protected ModelController createController (
        Properties props, Spatial target)
    {
        // attempt to create an instance of the controller
        ModelController ctrl;
        String cname = props.getProperty("class", "");
        try {
            ctrl = (ModelController)Class.forName(cname).newInstance();
        } catch (Exception e) {
            Log.warning("Error instantiating controller [class=" + cname +
                ", error=" + e + "].");
            return null;
        }
        ctrl.configure(props, target);
        return ctrl;
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
    
    /** A wrapper for the model properties providing access to the properties
     * of a node within the model. */
    protected static class NodeProperties extends Properties
    {
        public NodeProperties (Properties mprops, String name)
        {
            _mprops = mprops;
            _prefix = name + ".";
        }
        
        @Override // documentation inherited
        public String getProperty (String key)
        {
            return getProperty(key, null);
        }
        
        @Override // documentation inherited
        public String getProperty (String key, String defaultValue)
        {
            return _mprops.getProperty(_prefix + key,
                _mprops.getProperty(key, defaultValue));
        }
        
        /** The properties of the model. */
        protected Properties _mprops;
        
        /** The node prefix. */
        protected String _prefix;
    }
}
