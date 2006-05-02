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

import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import java.util.ArrayList;
import java.util.HashSet;

import com.jme.math.Matrix4f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.CloneCreator;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;

import com.threerings.jme.Log;

/**
 * A {@link Node} with a serialization mechanism tailored to stored models.
 */
public class ModelNode extends Node
    implements Externalizable, ModelSpatial
{
    /**
     * No-arg constructor for deserialization.
     */
    public ModelNode ()
    {
        super("node");
    }
    
    /**
     * Standard constructor.
     */
    public ModelNode (String name)
    {
        super(name);
    }
    
    /**
     * Recursively searches the scene graph rooted at this node for a
     * node with the provided name.
     */
    public Spatial getDescendant (String name)
    {
        for (int ii = 0, nn = getQuantity(); ii < nn; ii++) {
            Spatial child = getChild(ii);
            if (child.getName().equals(name)) {
                return child;
            } else if (child instanceof ModelNode) {
                child = ((ModelNode)child).getDescendant(name);
                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns a reference to the model space transform of the node.
     */
    public Matrix4f getModelTransform ()
    {
        return _modelTransform;
    }
    
    @Override // documentation inherited
    public void updateWorldData (float time)
    {
        // we use locked bounds as an indication that we can skip the update
        // altogether
        if ((lockedMode & LOCKED_BOUNDS) == 0) {
            super.updateWorldData(time);
        }
    }
    
    @Override // documentation inherited
    public void updateWorldBound ()
    {
        // if the node is culled, there are no mesh descendants and thus no
        // bounds
        if (cullMode != CULL_ALWAYS) {
            super.updateWorldBound();
        }
    }
    
    @Override // documentation inherited
    public void updateWorldVectors ()
    {
        super.updateWorldVectors();
        if (parent instanceof ModelNode) {
            setTransform(getLocalTranslation(), getLocalRotation(),
                getLocalScale(), _localTransform);
            ((ModelNode)parent).getModelTransform().mult(_localTransform,
                _modelTransform);
            
        } else {
            _modelTransform.loadIdentity();
        }
    }
    
    @Override // documentation inherited
    public Spatial putClone (Spatial store, CloneCreator properties)
    {
        ModelNode mstore = (ModelNode)properties.originalToCopy.get(this);
        if (mstore != null) {
            return mstore;
        } else if (store == null) {
            mstore = new ModelNode(getName());
        } else {
            mstore = (ModelNode)store;
        }
        super.putClone(mstore, properties);
        mstore.cullMode = cullMode;
        return mstore;
    }
    
    // documentation inherited from interface Externalizable
    public void writeExternal (ObjectOutput out)
        throws IOException
    {
        out.writeUTF(getName());
        out.writeObject(getLocalTranslation());
        out.writeObject(getLocalRotation());
        out.writeObject(getLocalScale());
        out.writeObject(getChildren());
    }
    
    // documentation inherited from interface Externalizable
    public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        setName(in.readUTF());
        setLocalTranslation((Vector3f)in.readObject());
        setLocalRotation((Quaternion)in.readObject());
        setLocalScale((Vector3f)in.readObject());
        ArrayList children = (ArrayList)in.readObject();
        for (int ii = 0, nn = children.size(); ii < nn; ii++) {
            attachChild((Spatial)children.get(ii));
        }
    }
    
    // documentation inherited from interface ModelSpatial
    public void expandModelBounds ()
    {
        for (Object child : getChildren()) {
            if (child instanceof ModelSpatial) {
                ((ModelSpatial)child).expandModelBounds();
            }
        }
    }
    
    // documentation inherited from interface ModelSpatial
    public void setReferenceTransforms ()
    {
        updateWorldVectors();
        for (Object child : getChildren()) {
            if (child instanceof ModelSpatial) {
                ((ModelSpatial)child).setReferenceTransforms();
            }       
        }
    }
    
    // documentation inherited from interface ModelSpatial
    public void lockStaticMeshes (
        Renderer renderer, boolean useVBOs, boolean useDisplayLists)
    {
        for (Object child : getChildren()) {
            if (child instanceof ModelSpatial) {
                ((ModelSpatial)child).lockStaticMeshes(renderer, useVBOs,
                    useDisplayLists);
            }
        }
    }
    
    // documentation inherited from interface ModelSpatial
    public void resolveTextures (TextureProvider tprov)
    {
        for (Object child : getChildren()) {
            if (child instanceof ModelSpatial) {
                ((ModelSpatial)child).resolveTextures(tprov);
            }
        }
    }
    
    // documentation inherited from interface ModelSpatial
    public void writeBuffers (FileChannel out)
        throws IOException
    {
        for (Object child : getChildren()) {
            if (child instanceof ModelSpatial) {
                ((ModelSpatial)child).writeBuffers(out);
            }
        }
    }
  
    // documentation inherited from interface ModelSpatial
    public void readBuffers (FileChannel in)
        throws IOException
    {
        for (Object child : getChildren()) {
            if (child instanceof ModelSpatial) {
                ((ModelSpatial)child).readBuffers(in);
            }
        }
    }
    
    // documentation inherited from interface ModelSpatial
    public void sliceBuffers (MappedByteBuffer map)
    {
        for (Object child : getChildren()) {
            if (child instanceof ModelSpatial) {
                ((ModelSpatial)child).sliceBuffers(map);
            }
        }
    }
    
    /**
     * Sets the cull state of any nodes that do not contain geometric
     * descendants to {@link CULL_ALWAYS} so that they don't waste
     * rendering time.
     *
     * @return true if this node should be drawn, false if it contains
     * no mesh descendants
     */
    protected boolean cullInvisibleNodes ()
    {
        boolean hasVisibleDescendants = false;
        for (int ii = 0, nn = getQuantity(); ii < nn; ii++) {
            Spatial child = getChild(ii);
            if (!(child instanceof ModelNode) ||
                ((ModelNode)child).cullInvisibleNodes()) {
                hasVisibleDescendants = true;
            }
        }
        setCullMode(hasVisibleDescendants ? CULL_INHERIT : CULL_ALWAYS);
        return hasVisibleDescendants;
    }
    
    /**
     * Locks the transforms and bounds of this instance with the assumption
     * that the position will never change.
     *
     * @param targets the targets of the model's controllers, which determine
     * the subset of nodes that can be locked
     * @return true if this node is a target or contains any targets, otherwise
     * false
     */
    protected boolean lockInstance (HashSet<Spatial> targets)
    {
        updateWorldVectors();
        lockedMode |= LOCKED_TRANSFORMS;
        
        boolean containsTargets = false;
        for (int ii = 0, nn = getQuantity(); ii < nn; ii++) {
            Spatial child = getChild(ii);
            if (targets.contains(child) || (child instanceof ModelNode &&
                ((ModelNode)child).lockInstance(targets))) {
                containsTargets = true;
                
            } else if (child instanceof ModelMesh) {
                ((ModelMesh)child).lockInstance();
            }
        }
        if (containsTargets) {
            return true;
        } else {
            updateWorldBound();
            lockedMode |= LOCKED_BOUNDS;
            return false;
        }
    }
    
    /**
     * Sets a matrix to the transform defined by the given translation,
     * rotation, and scale values.
     */
    protected static Matrix4f setTransform (
        Vector3f translation, Quaternion rotation, Vector3f scale,
        Matrix4f result)
    {
        result.set(rotation);
        result.setTranslation(translation);
        
        result.m00 *= scale.x;
        result.m01 *= scale.y;
        result.m02 *= scale.z;
        
        result.m10 *= scale.x;
        result.m11 *= scale.y;
        result.m12 *= scale.z;
        
        result.m20 *= scale.x;
        result.m21 *= scale.y;
        result.m22 *= scale.z;
        
        return result;
    }
    
    /** The node's transform in local and model space. */
    protected Matrix4f _localTransform = new Matrix4f(),
        _modelTransform = new Matrix4f();

    private static final long serialVersionUID = 1;
}
