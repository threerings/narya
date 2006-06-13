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

import java.util.Arrays;
import com.samskivert.util.StringUtil;
import com.threerings.media.image.Colorization;

/**
 * The character descriptor object details the components that are
 * pieced together to create a single character image.
 */
public class CharacterDescriptor
{
    /**
     * Constructs a character descriptor.
     *
     * @param components the component ids of the individual components
     * that make up this character.
     * @param zations the colorizations to apply to each of the character
     * component images when compositing actions (an array of
     * colorizations for each component, elements of which can be null to
     * make it easier to support per-component specialized colorizations).
     * This can be null if image recolorization is not desired.
     */
    public CharacterDescriptor (int[] components, Colorization[][] zations)
    {
        _components = components;
        _zations = zations;
    }

    /**
     * Returns an array of the component identifiers comprising the
     * character described by this descriptor.
     */
    public int[] getComponentIds ()
    {
        return _components;
    }

    /**
     * Returns an array of colorization arrays to be applied to the
     * components when compositing action images (one array per
     * component).
     */
    public Colorization[][] getColorizations ()
    {
        return _zations;
    }

    /**
     * Updates the colorizations to be used by this character descriptor.
     */
    public void setColorizations (Colorization[][] zations)
    {
        _zations = zations;
    }

    /**
     * Compute a sensible hashcode for this object.
     */
    public int hashCode ()
    {
        int code = 0, clength = _components.length;
        for (int i = 0; i < clength; i++) {
            code ^= _components[i];
        }
        return code;
    }

    /**
     * Compares this character descriptor to another.
     */
    public boolean equals (Object other)
    {
        if (!(other instanceof CharacterDescriptor)) {
            return false;
        }

        // both the component ids and the colorizations must be equal
        CharacterDescriptor odesc = (CharacterDescriptor)other;
        if (!Arrays.equals(_components, odesc._components)) {
            return false;
        }

        Colorization[][] zations = odesc._zations;
        if (zations == null && _zations == null) {
            // if neither has colorizations, we're clear
            return true;

        } else if (zations == null || _zations == null) {
            // if one has colorizations whilst the other doesn't, they
            // can't be equal
            return false;
        }

        // otherwise, all of the colorizations must be equal as well
        int zlength = zations.length;
        if (zlength != _zations.length) {
            return false;
        }
        for (int ii = 0; ii < zlength; ii++) {
            if (!Arrays.equals(_zations[ii], zations[ii])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a string representation of this character descriptor.
     */
    public String toString ()
    {
        return "[cids=" + StringUtil.toString(_components) +
            ", colors=" + StringUtil.toString(_zations) + "]";
    }

    /** The component identifiers comprising the character. */
    protected int[] _components;

    /** The colorizations to apply when compositing this character. */
    protected Colorization[][] _zations;
}
