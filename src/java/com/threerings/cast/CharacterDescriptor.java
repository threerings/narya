//
// $Id: CharacterDescriptor.java,v 1.3 2001/11/01 01:40:42 shaper Exp $

package com.threerings.cast;

import com.samskivert.util.StringUtil;

/**
 * The character descriptor object details the components that are
 * pieced together to create a single character image.
 */
public class CharacterDescriptor
{
    /**
     * Constructs the character descriptor. 
     */
    public CharacterDescriptor (int components[])
    {
        _components = components;
    }

    /**
     * Returns an array of the component identifiers comprising the
     * character described by this descriptor.
     */
    public int[] getComponents ()
    {
        return _components;
    }

    /**
     * Returns a string representation of this character descriptor.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[").append(StringUtil.toString(_components));
        return buf.append("]").toString();
    }

    /** The component identifiers comprising the character. */
    protected int[] _components;
}
