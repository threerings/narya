//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.cast;

import java.io.Serializable;

import com.samskivert.util.StringUtil;

import com.threerings.media.sprite.Sprite;

/**
 * The character component represents a single component that can be
 * composited with other character components to generate an image
 * representing a complete character displayable in any of the eight
 * compass directions as detailed in the {@link Sprite} class direction
 * constants.
 */
public class CharacterComponent implements Serializable
{
    /** The unique component identifier. */
    public int componentId;

    /** The component's name. */
    public String name;

    /** The class of components to which this one belongs. */
    public ComponentClass componentClass;

    /**
     * Constructs a character component with the specified id of the
     * specified class.
     */
    public CharacterComponent (int componentId, String name,
                               ComponentClass compClass, FrameProvider fprov)
    {
        this.componentId = componentId;
        this.name = name;
        this.componentClass = compClass;
        _frameProvider = fprov;
    }

    /**
     * Returns the image frames for the specified action animation or null
     * if no animation for the specified action is available for this
     * component.
     */
    public ActionFrames getFrames (String action)
    {
        return _frameProvider.getFrames(this, action);
    }

    /**
     * Returns true if this component is equal to the other component. The
     * comparison is made on <code>componentId</code>.
     */
    public boolean equals (Object other)
    {
        if (other instanceof CharacterComponent) {
            return componentId == ((CharacterComponent)other).componentId;
        } else {
            return false;
        }
    }

    /**
     * Returns a string representation of this character component.
     */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }

    /** The entity from which we obtain our animation frames. */
    protected FrameProvider _frameProvider;

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 1;
}
