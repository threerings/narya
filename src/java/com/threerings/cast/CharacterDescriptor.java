//
// $Id: CharacterDescriptor.java,v 1.1 2001/10/26 01:17:21 shaper Exp $

package com.threerings.cast;

import com.samskivert.util.StringUtil;

/**
 * The character descriptor object contains all necessary information
 * on the character components that are composited together to create
 * a character image.
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
     * Returns a list of the component identifiers comprising the
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

    /** The array of component identifiers. */
    protected int _components[];
}
