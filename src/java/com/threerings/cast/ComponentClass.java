//
// $Id: ComponentClass.java,v 1.5 2002/10/15 21:01:39 ray Exp $

package com.threerings.cast;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Denotes a class of components to which {@link CharacterComponent}
 * objects belong. Examples include "Hat", "Head", and "Feet". A component
 * class dictates a component's rendering priority so that components can
 * be rendered in an order that causes them to overlap properly.
 */
public class ComponentClass implements Serializable
{
    /** The comparator used to sort component class objects in render
     * priority order. */
    public static final Comparator RENDER_COMP = new RenderComparator();

    /** The component class name. */
    public String name;

    /** The render priority. */
    public int renderPriority;

    /**
     * Classes with the same name are the same.
     */
    public boolean equals (Object other)
    {
        if (other instanceof ComponentClass) {
            return name.equals(((ComponentClass)other).name);
        } else {
            return false;
        }
    }

    /**
     * Hashcode is based on component class name.
     */
    public int hashCode ()
    {
        return name.hashCode();
    }

    /**
     * Returns a string representation of this component class.
     */
    public String toString ()
    {
        return "[name=" + name + ", renderPriority=" + renderPriority + "]";
    }

    /**
     * The comparator used to sort {@link CharacterComponent} instances in
     * render priority order so that compositing components into a single
     * character image can be done in the proper order.
     */
    protected static class RenderComparator implements Comparator
    {
        // documentation inherited
        public int compare (Object a, Object b)
        {
            if (!(a instanceof CharacterComponent) ||
                !(b instanceof CharacterComponent)) {
                return -1;
            }

            CharacterComponent ca = (CharacterComponent)a;
            CharacterComponent cb = (CharacterComponent)b;
            return (ca.componentClass.renderPriority -
                    cb.componentClass.renderPriority);
        }
    }

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 1;
}
