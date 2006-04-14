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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;

import com.threerings.jme.Log;
import com.threerings.jme.model.Model;

/**
 * A basic representation for keyframe animations.
 */
public class AnimationDef
{
    /** A single frame of the animation. */
    public static class FrameDef
    {
        /** Transform for affected nodes. */
        public ArrayList<TransformDef> transforms =
            new ArrayList<TransformDef>();
        
        public void addTransform (TransformDef transform)
        {
            transforms.add(transform);
        }
        
        /** Adds all transform targets in this frame to the supplied set. */
        public void addTransformTargets (
            HashMap<String, Spatial> nodes, HashSet<Spatial> targets)
        {
            for (int ii = 0, nn = transforms.size(); ii < nn; ii++) {
                String name = transforms.get(ii).name;
                Spatial target = nodes.get(name);
                if (target != null) {
                    targets.add(target);
                } else {
                    Log.warning("Missing animation target [name=" + name +
                        "].");
                }
            }
        }
        
        /** Returns the array of transforms for this frame. */
        public Model.Transform[] getTransforms (Spatial[] targets)
        {
            Model.Transform[] mtransforms =
                new Model.Transform[targets.length];
            for (int ii = 0; ii < targets.length; ii++) {
                mtransforms[ii] = getTransform(targets[ii]);
            }
            return mtransforms;
        }
        
        /** Returns the transform for the supplied target. */
        protected Model.Transform getTransform (Spatial target)
        {
            String name = target.getName();
            for (int ii = 0, nn = transforms.size(); ii < nn; ii++) {
                TransformDef transform = transforms.get(ii);
                if (name.equals(transform.name)) {
                    return transform.getTransform();
                }
            }
            return null;
        }
    }
    
    /** A transform for a single node. */
    public static class TransformDef
    {
        /** The name of the affected node. */
        public String name;
        
        /** The transformation parameters. */
        public float[] translation;
        public float[] rotation;
        public float[] scale;
        
        /** Returns the live transform object. */
        public Model.Transform getTransform ()
        {
            return new Model.Transform(
                new Vector3f(translation[0], translation[1], translation[2]),
                new Quaternion(rotation[0], rotation[1], rotation[2],
                    rotation[3]),
                new Vector3f(scale[0], scale[1], scale[2]));
        }
    }
    
    /** The individual frames of the animation. */
    public ArrayList<FrameDef> frames = new ArrayList<FrameDef>();
    
    public void addFrame (FrameDef frame)
    {
        frames.add(frame);
    }
    
    /**
     * Creates the "live" animation object that will be serialized with the
     * object.
     *
     * @param props the model properties
     * @param nodes the nodes in the model, mapped by name
     */
    public Model.Animation createAnimation (
        Properties props, HashMap<String, Spatial> nodes)
    {
        // find all affected nodes
        HashSet<Spatial> targets = new HashSet<Spatial>();
        for (int ii = 0, nn = frames.size(); ii < nn; ii++) {
            frames.get(ii).addTransformTargets(nodes, targets);
        }
        
        // collect all transforms
        Model.Animation anim = new Model.Animation();
        anim.transformTargets = targets.toArray(new Spatial[targets.size()]);
        anim.transforms = new Model.Transform[frames.size()][targets.size()];
        for (int ii = 0; ii < anim.transforms.length; ii++) {
            anim.transforms[ii] =
                frames.get(ii).getTransforms(anim.transformTargets);
        }
        return anim;
    }
}
