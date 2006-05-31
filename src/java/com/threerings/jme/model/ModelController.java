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

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;

import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;

import com.samskivert.util.StringUtil;

/**
 * The superclass of procedural animation controllers for models.
 */
public abstract class ModelController extends Controller
    implements Externalizable
{
    /**
     * Configures this controller based on the supplied (sub-)properties and
     * controller target.
     */
    public void configure (Properties props, Spatial target)
    {
        _target = target;
        String[] anims = StringUtil.parseStringArray(
            props.getProperty("animations", ""));
        if (anims.length == 0) {
            return;
        }
        _animations = new HashSet<String>();
        Collections.addAll(_animations, anims);
    }
    
    /**
     * Returns a reference to the controller's target.
     */
    public Spatial getTarget ()
    {
        return _target;
    }
    
    /**
     * Resolves any textures required by the controller.
     */
    public void resolveTextures (TextureProvider tprov)
    {
    }
    
    /**
     * Initializes this controller.
     */
    public void init (Model model)
    {
        model.addAnimationObserver(_animobs);
        if (_animations != null) {
            setActive(false);
        }
    }
    
    /**
     * Creates or populates and returns a clone of this object using the given
     * clone properties.
     *
     * @param store an instance of this class to populate, or <code>null</code>
     * to create a new instance
     */
    public Controller putClone (
        Controller store, Model.CloneCreator properties)
    {
        if (store == null) {
            return null;
        }
        ModelController mstore = (ModelController)store;
        mstore._target = ((ModelSpatial)_target).putClone(null, properties);
        mstore._animations = _animations;
        return mstore;
    }
    
    // documentation inherited from interface Externalizable
    public void writeExternal (ObjectOutput out)
        throws IOException
    {
        out.writeObject(_target);
        out.writeObject(_animations);
    }
    
    // documentation inherited from interface Externalizable
    public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        _target = (Spatial)in.readObject();
        _animations = (HashSet<String>)in.readObject();
    }
    
    /**
     * Called when an animation is started on the model.
     */
    protected void animationStarted (String anim)
    {
        if (_animations != null && _animations.contains(anim)) {
            setActive(true);
        }
    }
    
    /**
     * Called when an animation is stopped on the model.
     */
    protected void animationStopped (String anim)
    {
        if (_animations != null) {
            setActive(false);
        }
    }
    
    /** The target to control. */
    protected Spatial _target;
    
    /** The animations for which this controller should be active, or
     * <code>null</code> for all of them. */
    protected HashSet<String> _animations;
    
    /** Listens to the model's animation state. */
    protected Model.AnimationObserver _animobs =
        new Model.AnimationObserver() {
        public boolean animationStarted (Model model, String anim) {
            ModelController.this.animationStarted(anim);
            return true;
        }
        public boolean animationCompleted (Model model, String anim) {
            animationStopped(anim);
            return true;
        }
        public boolean animationCancelled (Model model, String anim) {
            animationStopped(anim);
            return true;
        }
    };
    
    private static final long serialVersionUID = 1;
}
