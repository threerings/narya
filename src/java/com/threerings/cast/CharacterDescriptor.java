//
// $Id: CharacterDescriptor.java,v 1.4 2001/11/27 08:09:34 mdb Exp $

package com.threerings.cast;

import java.util.Arrays;
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
    public CharacterDescriptor (int[] components)
    {
        _components = components;
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
        if (other instanceof CharacterDescriptor) {
            return Arrays.equals(_components,
                                 ((CharacterDescriptor)other)._components);
        } else {
            return false;
        }
    }

    /**
     * Returns a string representation of this character descriptor.
     */
    public String toString ()
    {
        return "[cids=" + StringUtil.toString(_components) + "]";
    }

    /** The component identifiers comprising the character. */
    protected int[] _components;
}
