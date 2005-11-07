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

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * An ant task for converting cube maps (sky boxes) to sphere maps.
 */
public class BuildSphereMapTask extends Task
{
    public void setFront (File front)
    {
        _front = front;
    }

    public void setBack (File back)
    {
        _back = back;
    }
    
    public void setLeft (File left)
    {
        _left = left;
    }
    
    public void setRight (File right)
    {
        _right = right;
    }
    
    public void setUp (File up)
    {
        _up = up;
    }
    
    public void setTarget (File target)
    {
        _target = target;
    }
    
    public void setSize (int size)
    {
        _size = size;
    }
    
    public void execute () throws BuildException
    {
        try {
            BuildSphereMap.execute(_front, _back, _left, _right, _up, _target,
                _size);
        
        } catch (IOException e) {
            throw new BuildException("Failure building sphere map", e);
        }
    }

    /** The files representing the sides of the cube map and the target. */
    protected File _front, _back, _left, _right, _up, _target;
    
    /** The size of the target image. */
    protected int _size;
}
