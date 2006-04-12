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

/**
 * A basic representation for keyframe animations.
 */
public class AnimationDef
{
    /** A single frame of the animation. */
    public static class Frame
    {
        /** Transforms for affected nodes. */
        public ArrayList<Transform> transforms = new ArrayList<Transform>();
        
        public void addTransform (Transform transform)
        {
            transforms.add(transform);
        }
    }
    
    /** A transform for a single node. */
    public static class Transform
    {
        /** The name of the node to transform. */
        public String name;
        
        /** The transformation parameters. */
        public float[] translation;
        public float[] rotation;
        public float[] scale;
    }
    
    /** The individual frames of the animation. */
    public ArrayList<Frame> frames = new ArrayList<Frame>();
    
    public void addFrame (Frame frame)
    {
        frames.add(frame);
    }
}
