//
// $Id: CharacterDescriptor.java,v 1.2 2001/10/30 16:16:01 shaper Exp $

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
    public CharacterDescriptor (ComponentType type, int components[])
    {
        _type = type;
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
     * Returns the {@link ComponentType} of the character's components.
     */
    public ComponentType getType ()
    {
        return _type;
    }

    /**
     * Returns a string representation of this character descriptor.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[type=").append(_type);
        buf.append(StringUtil.toString(_components));
        return buf.append("]").toString();
    }

    /** The array of component identifiers. */
    protected int _components[];

    /** The component type of the character's components. */
    protected ComponentType _type;
}
