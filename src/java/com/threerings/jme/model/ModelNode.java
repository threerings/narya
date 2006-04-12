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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
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
    }
    
    /**
     * Standard constructor.
     */
    public ModelNode (String name)
    {
        super(name);
    }
    
    /**
     * Recursively sets the reference transforms for any {@link BoneNode}s in
     * the model.
     */
    public void setReferenceTransforms ()
    {
        for (Object child : getChildren()) {
            if (child instanceof ModelNode) {
                ((ModelNode)child).setReferenceTransforms();
            }       
        }
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
        throws IOException
    {
        setName(in.readUTF());
        try {
            setLocalTranslation((Vector3f)in.readObject());
            setLocalRotation((Quaternion)in.readObject());
            setLocalScale((Vector3f)in.readObject());
            ArrayList children = (ArrayList)in.readObject();
            for (int ii = 0, nn = children.size(); ii < nn; ii++) {
                attachChild((Spatial)children.get(ii));
            }
        } catch (ClassNotFoundException e) {
            Log.warning("Encounted unknown class [node=" + getName() +
                ", exception=" + e + "].");
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
    
    private static final long serialVersionUID = 1;
}
