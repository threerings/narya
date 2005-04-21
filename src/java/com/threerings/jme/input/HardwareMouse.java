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

package com.threerings.jme.input;

import com.jme.input.Mouse;
import com.jme.math.Vector3f;

/**
 * Implements the {@link Mouse} methods by simply querying the position of
 * the hardware mouse under the assumption that it will not be turned off.
 */
public class HardwareMouse extends Mouse
{
    public HardwareMouse (String name)
    {
        super(name);
    }

    public void update ()
    {
        update(true);
    }

    public void update (boolean updateState)
    {
    }

    public Vector3f getHotSpotPosition() {
        hotSpotLocation.x = mouse.getXAbsolute();
        hotSpotLocation.y = mouse.getYAbsolute();
        return hotSpotLocation;
    }
}
