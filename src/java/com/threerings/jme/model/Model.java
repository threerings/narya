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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import java.util.HashMap;
import java.util.Properties;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;

import com.threerings.jme.Log;

/**
 * The base node for models.
 */
public class Model extends ModelNode
{
    /** An animation for the model. */
    public static class Animation
        implements Serializable
    {
        /** The transformation targets of the animation. */
        public Spatial[] transformTargets;
        
        /** The animation transforms (one transform per target per frame). */
        public transient Transform[][] transforms;
        
        private void writeObject (ObjectOutputStream out)
            throws IOException
        {
            out.defaultWriteObject();
            out.writeInt(transforms.length);
            for (int ii = 0; ii < transforms.length; ii++) {
                for (int jj = 0; jj < transformTargets.length; jj++) {
                    transforms[ii][jj].writeExternal(out);
                }
            }
        }

        private void readObject (ObjectInputStream in)
            throws IOException, ClassNotFoundException
        {
            in.defaultReadObject();
            transforms = new Transform[in.readInt()][transformTargets.length];
            for (int ii = 0; ii < transforms.length; ii++) {
                for (int jj = 0; jj < transformTargets.length; jj++) {
                    transforms[ii][jj] = new Transform(new Vector3f(),
                        new Quaternion(), new Vector3f());
                    transforms[ii][jj].readExternal(in);
                }
            }
        }
        
        private static final long serialVersionUID = 1;
    }
    
    /** A frame element that manipulates the target's transform. */
    public static final class Transform
        implements Externalizable
    {
        public Transform (
            Vector3f translation, Quaternion rotation, Vector3f scale)
        {
            _translation = translation;
            _rotation = rotation;
            _scale = scale;
        }
        
        /**
         * Blends between this transform and the next, applying the result to
         * the given target.
         *
         * @param alpha the blend factor: 0.0 for entirely this frame, 1.0 for
         * entirely the next
         */
        public void blend (Transform next, float alpha, Spatial target)
        {
            target.getLocalTranslation().interpolate(_translation,
                next._translation, alpha);
            target.getLocalRotation().slerp(_rotation, next._rotation, alpha);
            target.getLocalScale().interpolate(_scale, next._scale, alpha);
        }
        
        // documentation inherited from interface Externalizable
        public void writeExternal (ObjectOutput out)
            throws IOException
        {
            _translation.writeExternal(out);
            _rotation.writeExternal(out);
            _scale.writeExternal(out);
        }
        
        // documentation inherited from interface Externalizable
        public void readExternal (ObjectInput in)
            throws IOException, ClassNotFoundException
        {
            _translation.readExternal(in);
            _rotation.readExternal(in);
            _scale.readExternal(in);
        }

        /** The transform at this frame. */
        protected Vector3f _translation, _scale;
        protected Quaternion _rotation;
        
        private static final long serialVersionUID = 1;
    }
    
    /**
     * Attempts to read a model from the specified file.
     *
     * @param map if true, map buffers into memory directly from the
     * file
     */
    public static Model readFromFile (File file, boolean map)
        throws IOException
    {
        // read the serialized model and its children
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Model model;
        try {
            model = (Model)ois.readObject();
        } catch (ClassNotFoundException e) {
            Log.warning("Encountered unknown class [error=" + e + "].");
            return null;
        }
        
        // then either read or map the buffers
        FileChannel fc = fis.getChannel();
        if (map) {
            long pos = fc.position();
            MappedByteBuffer buf = fc.map(FileChannel.MapMode.READ_ONLY,
                pos, fc.size() - pos);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            model.sliceBuffers(buf);
        } else {
            model.readBuffers(fc);
        }
        ois.close();
        return model;
    }
    
    /**
     * No-arg constructor for deserialization.
     */
    public Model ()
    {
    }
    
    /**
     * Standard constructor.
     */
    public Model (String name, Properties props)
    {
        super(name);
        _props = props;
    }
    
    /**
     * Returns a reference to the properties of the model.
     */
    public Properties getProperties ()
    {
        return _props;
    }
    
    /**
     * Adds an animation to the model's library.
     */
    public void addAnimation (String name, Animation anim)
    {
        if (_anims == null) {
            _anims = new HashMap<String, Animation>();
        }
        _anims.put(name, anim);
    }
    
    /**
     * Returns the names of the model's animations.
     */
    public String[] getAnimations ()
    {
        return (_anims == null) ? new String[0] :
            _anims.keySet().toArray(new String[_anims.size()]);
    }
    
    /**
     * Starts the named animation.
     */
    public void startAnimation (String name)
    {
        _anim = _anims.get(name);
        if (_anim == null) {
            Log.warning("Requested unknown animation [name=" +
                name + "].");
            return;
        }
        _fidx = -1;
    }
    
    /**
     * Stops the currently running animation.
     */
    public void stopAnimation ()
    {
        _anim = null;
    }
    
    /**
     * Writes this model out to a file.
     */
    public void writeToFile (File file)
        throws IOException
    {
        // start out by writing this node and its children
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        
        // now traverse the scene graph appending buffers to the
        // end of the file
        writeBuffers(fos.getChannel());
        oos.close();
    }
    
    @Override // documentation inherited
    public void writeExternal (ObjectOutput out)
        throws IOException
    {
        super.writeExternal(out);
        out.writeObject(_props);
        out.writeObject(_anims);
    }
    
    @Override // documentation inherited
    public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        super.readExternal(in);
        _props = (Properties)in.readObject();
        _anims = (HashMap<String, Animation>)in.readObject();
    }
    
    @Override // documentation inherited
    public void updateWorldData (float time)
    {
        if (_anim != null) {
            updateAnimation(time);
        }
        
        // update children
        super.updateWorldData(time);
    }
    
    /**
     * Updates the model's state according to the current animation.
     */
    protected void updateAnimation (float time)
    {
        
    }
    
    /** The model properties. */
    protected Properties _props;
    
    /** The model animations. */
    protected HashMap<String, Animation> _anims;
    
    /** The currently running animation, or <code>null</code> for none. */
    protected Animation _anim;
    
    /** The last frame index. */
    protected int _fidx;
    
    /** The time corresponding to the last frame. */
    protected float _ftime;
    
    /** Identifies a transform frame element. */
    protected static final byte TRANSFORM_ELEMENT = 0;
    
    private static final long serialVersionUID = 1;
}
