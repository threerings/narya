//
// $Id: ComponentClass.java,v 1.1 2001/10/30 16:16:01 shaper Exp $

package com.threerings.cast;

import java.util.Comparator;

/**
 * The component class object denotes the particular class of
 * components that a {@link CharacterComponent} object belongs to.
 * Examples of feasible component classes might include "Hat", "Head",
 * or "Feet".
 */
public class ComponentClass
{
    /** The comparator used to sort component class objects in render
     * priority order. */
    public static final Comparator RENDER_COMP = new RenderComparator();

    /** The unique component class identifier. */
    public int clid;

    /** The component class name. */
    public String name;

    /** The render priority. */
    public int render;

    /**
     * Returns a string representation of this component class.
     */
    public String toString ()
    {
        return "[clid=" + clid + ", name=" + name + "]";
    }

    /**
     * The comparator used to sort component class objects in render
     * priority order so that compositing components into a single
     * character image can be done in the proper order.
     */
    protected static class RenderComparator implements Comparator
    {
        // documentation inherited
        public int compare (Object a, Object b)
        {
            if (!(a instanceof ComponentClass) ||
                !(b instanceof ComponentClass)) {
                return -1;
            }

            ComponentClass ca = (ComponentClass)a;
            ComponentClass cb = (ComponentClass)b;

            if (ca.render < cb.render) {
                return -1;
            } else if (ca.render == cb.render) {
                return 0;
            } else {
                return 1;
            }
        }

        // documentation inherited
        public boolean equals (Object obj)
        {
	    return (obj == this);
        }
    }
}
