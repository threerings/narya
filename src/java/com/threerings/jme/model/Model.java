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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.samskivert.util.ObserverList;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.CloneCreator;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;

import com.threerings.jme.Log;

/**
 * The base node for models.
 */
public class Model extends ModelNode
{
    /** Lets listeners know when animations are completed (which only happens
     * for non-repeating animations) or cancelled. */
    public interface AnimationObserver
    {
        /**
         * Called when an animation has started.
         *
         * @return true to remain on the observer list, false to remove self
         */
        public boolean animationStarted (Model model, String anim);
        
        /**
         * Called when a non-repeating animation has finished.
         *
         * @return true to remain on the observer list, false to remove self
         */
        public boolean animationCompleted (Model model, String anim);
        
        /**
         * Called when an animation has been cancelled.
         *
         * @return true to remain on the observer list, false to remove self
         */
        public boolean animationCancelled (Model model, String anim);
    }
    
    /** An animation for the model. */
    public static class Animation
        implements Serializable
    {
        /** The rate of the animation in frames per second. */
        public int frameRate;
        
        /** The animation repeat type ({@link Controller#RT_CLAMP},
         * {@link Controller#RT_CYCLE}, or {@link Controller#RT_WRAP}). */
        public int repeatType;
        
        /** The transformation targets of the animation. */
        public Spatial[] transformTargets;
        
        /** The animation transforms (one transform per target per frame). */
        public transient Transform[][] transforms;
        
        /**
         * Returns this animation's duration in seconds.
         */
        public float getDuration ()
        {
            return (float)transforms.length / frameRate;
        }
        
        /**
         * Rebinds this animation for a prototype instance.
         *
         * @param pnodes a mapping from prototype nodes to instance nodes
         */
        public Animation rebind (HashMap pnodes)
        {
            Animation anim = new Animation();
            anim.frameRate = frameRate;
            anim.repeatType = repeatType;
            anim.transforms = transforms;
            anim.transformTargets = new Spatial[transformTargets.length];
            for (int ii = 0; ii < transformTargets.length; ii++) {
                anim.transformTargets[ii] =
                    (Spatial)pnodes.get(transformTargets[ii]);
            }
            return anim;
        }
        
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
        
        // initialize the model as a prototype
        model.initPrototype();
        
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
     * Initializes this model as prototype.  Only necessary when the prototype
     * was not loaded through {@link #readFromFile}.
     */
    public void initPrototype ()
    {
        setReferenceTransforms();
        initInstance();
    }
    
    /**
     * Adds an animation to the model's library.  This should only be called by
     * the model compiler.
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
    public String[] getAnimationNames ()
    {
        if (_prototype != null) {
            return _prototype.getAnimationNames();
        }
        return (_anims == null) ? new String[0] :
            _anims.keySet().toArray(new String[_anims.size()]);
    }
    
    /**
     * Checks whether the unit has an animation with the given name.
     */
    public boolean hasAnimation (String name)
    {
        if (_prototype != null) {
            return _prototype.hasAnimation(name);
        }
        return (_anims == null) ? false : _anims.containsKey(name);
    }
    
    /**
     * Starts the named animation.
     *
     * @return a reference to the started animation
     */
    public Animation startAnimation (String name)
    {
        Animation anim = getAnimation(name);
        if (anim == null) {
            return null;
        }
        _anim = anim;
        _animName = name;
        _fidx = 0;
        _nidx = 1;
        _fdir = +1;
        _elapsed = 0f;
        _animObservers.apply(new AnimStartedOp(_animName));
        return anim;
    }
    
    /**
     * Gets a reference to the animation with the given name.
     */
    public Animation getAnimation (String name)
    {
        Animation anim = _anims.get(name);
        if (anim != null) {
            return anim;
        }
        if (_prototype != null) {
            Animation panim = _prototype._anims.get(name);
            if (panim != null) {
                _anims.put(name, anim = panim.rebind(_pnodes));
                return anim;
            }
        }
        Log.warning("Requested unknown animation [name=" + name + "].");
        return null;
    }
    
    /**
     * Stops the currently running animation.
     */
    public void stopAnimation ()
    {
        if (_anim == null) {
            return;
        }
        _anim = null;
        _animObservers.apply(new AnimCancelledOp(_animName));
    }
    
    /**
     * Sets the animation speed, which acts as a multiplier for the frame rate
     * of each animation.
     */
    public void setAnimationSpeed (float speed)
    {
        _animSpeed = speed;
    }
    
    /**
     * Returns the currently configured animation speed.
     */
    public float getAnimationSpeed ()
    {
        return _animSpeed;
    }
    
    /**
     * Adds an animation observer.
     */
    public void addAnimationObserver (AnimationObserver obs)
    {
        _animObservers.add(obs);
    }
    
    /**
     * Removes an animation observer.
     */
    public void removeAnimationObserver (AnimationObserver obs)
    {
        _animObservers.remove(obs);
    }
    
    /**
     * Returns a reference to the node that contains this model's emissions
     * (in world space, so the emissions do not move with the model).  This
     * node is created and added when this method is first called.
     */
    public Node getEmissionNode ()
    {
        if (_emissionNode == null) {
            attachChild(_emissionNode = new Node("emissions"));
            _emissionNode.setTransformable(false);
        }
        return _emissionNode;
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
        out.writeObject(getControllers());
    }
    
    @Override // documentation inherited
    public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        super.readExternal(in);
        _props = (Properties)in.readObject();
        _anims = (HashMap<String, Animation>)in.readObject();
        ArrayList controllers = (ArrayList)in.readObject();
        for (Object ctrl : controllers) {
            addController((Controller)ctrl);
        }
    }

    @Override // documentation inherited
    public void resolveTextures (TextureProvider tprov)
    {
        super.resolveTextures(tprov);
        for (Object ctrl : getControllers()) {
            if (ctrl instanceof ModelController) {
                ((ModelController)ctrl).resolveTextures(tprov);
            }
        }
    }
    
    /**
     * Creates and returns a new instance of this model.
     */    
    public Model createInstance ()
    {
        if (_prototype != null) {
            return _prototype.createInstance();
        }
        if (_ccreator == null) {
            // allow adding and removing properties at any time
            _ccreator = new CloneCreator(this) {
                public void addProperty (String name) {
                    props.put(name, Boolean.TRUE);
                }
                public void removeProperty (String name) {
                    props.remove(name);
                }
            };
            _ccreator.addProperty("vertices");
            _ccreator.addProperty("colors");
            _ccreator.addProperty("normals");
            _ccreator.addProperty("texcoords");
            _ccreator.addProperty("vboinfo");
            _ccreator.addProperty("indices");
            _ccreator.addProperty("obbtree");
            _ccreator.addProperty("displaylistid");
            _ccreator.addProperty("bound");
        }
        Model instance = (Model)_ccreator.createCopy();
        instance.initInstance();
        return instance;
    }

    @Override // documentation inherited
    public Spatial putClone (Spatial store, CloneCreator properties)
    {
        Model mstore;
        if (store == null) {
            mstore = new Model(getName(), _props);
        } else {
            mstore = (Model)store;
        }
        super.putClone(mstore, properties);
        mstore._prototype = this;
        if (_anims != null) {
            mstore._anims = new HashMap<String, Animation>();
        }
        mstore._pnodes = properties.originalToCopy;
        return mstore;
    }
    
    @Override // documentation inherited
    public void updateWorldData (float time)
    {
        // slow evvvverything down by the animation speed
        time *= _animSpeed;
        if (_anim != null) {
            updateAnimation(time);
        }
        
        // update children
        super.updateWorldData(time);
    }
    
    /**
     * Initializes the per-instance state of this model.
     */
    protected void initInstance ()
    {
        // initialize the controllers
        for (Object ctrl : getControllers()) {
            if (ctrl instanceof ModelController) {
                ((ModelController)ctrl).init(this);
            }
        }
    }
    
    /**
     * Updates the model's state according to the current animation.
     */
    protected void updateAnimation (float time)
    {
        // advance the frame counter if necessary
        while (_elapsed > 1f) {
            advanceFrameCounter();
            _elapsed -= 1f;
        }
        
        // update the target transforms
        Spatial[] targets = _anim.transformTargets;
        Transform[] xforms = _anim.transforms[_fidx],
            nxforms = _anim.transforms[_nidx];
        for (int ii = 0; ii < targets.length; ii++) {
            xforms[ii].blend(nxforms[ii], _elapsed, targets[ii]);
        }
        
        // if the next index is the same as this one, we are finished
        if (_fidx == _nidx) {
            _anim = null;
            _animObservers.apply(new AnimCompletedOp(_animName));
            return;
        }
        
        _elapsed += (time * _anim.frameRate);
    }
    
    /**
     * Advances the frame counter by one frame.
     */
    protected void advanceFrameCounter ()
    {
        _fidx = _nidx;
        int nframes = _anim.transforms.length;
        if (_anim.repeatType == Controller.RT_CLAMP) {
            _nidx = Math.min(_nidx + 1, nframes - 1);
            
        } else if (_anim.repeatType == Controller.RT_WRAP) {
            _nidx = (_nidx + 1) % nframes;
            
        } else { // _anim.repeatType == Controller.RT_CYCLE
            if ((_nidx + _fdir) < 0 || (_nidx + _fdir) >= nframes) {
                _fdir *= -1; // reverse direction
            }
            _nidx += _fdir;
        }
    }
    
    /** A reference to the prototype, or <code>null</code> if this is a
     * prototype. */
    protected Model _prototype;
    
    /** For prototype models, a customized clone creator used to generate
     * instances. */
    protected CloneCreator _ccreator;
    
    /** For instances, maps prototype nodes to their corresponding instance
     * nodes. */
    protected HashMap _pnodes;
    
    /** The model properties. */
    protected Properties _props;
    
    /** The model animations. */
    protected HashMap<String, Animation> _anims;
    
    /** The currently running animation, or <code>null</code> for none. */
    protected Animation _anim;
    
    /** The name of the currently running animation, if any. */
    protected String _animName;
    
    /** The current animation speed multiplier. */
    protected float _animSpeed = 1f;
    
    /** The index of the current and next frames. */
    protected int _fidx, _nidx;
    
    /** The direction for wrapping animations (+1 forward, -1 backward). */
    protected int _fdir;
    
    /** The frame portion elapsed since the start of the current frame. */
    protected float _elapsed;
    
    /** The child node that contains the model's emissions in world space. */
    protected Node _emissionNode;
    
    /** Animation completion listeners. */
    protected ObserverList<AnimationObserver> _animObservers =
        new ObserverList<AnimationObserver>(ObserverList.FAST_UNSAFE_NOTIFY);
    
    /** Used to notify observers of animation initiation. */
    protected class AnimStartedOp
        implements ObserverList.ObserverOp<AnimationObserver>
    {
        public AnimStartedOp (String name)
        {
            _name = name;
        }
        
        public boolean apply (AnimationObserver obs)
        {
            return obs.animationStarted(Model.this, _name);
        }
        
        /** The name of the animation started. */
        protected String _name;
    }
    
    /** Used to notify observers of animation completion. */
    protected class AnimCompletedOp
        implements ObserverList.ObserverOp<AnimationObserver>
    {
        public AnimCompletedOp (String name)
        {
            _name = name;
        }
        
        public boolean apply (AnimationObserver obs)
        {
            return obs.animationCompleted(Model.this, _name);
        }
        
        /** The name of the animation completed. */
        protected String _name;
    }
    
    /** Used to notify observers of animation cancellation. */
    protected class AnimCancelledOp
        implements ObserverList.ObserverOp<AnimationObserver>
    {
        public AnimCancelledOp (String name)
        {
            _name = name;
        }
        
        public boolean apply (AnimationObserver obs)
        {
            return obs.animationCancelled(Model.this, _name);
        }
        
        /** The name of the animation cancelled. */
        protected String _name;
    }
    
    private static final long serialVersionUID = 1;
}
