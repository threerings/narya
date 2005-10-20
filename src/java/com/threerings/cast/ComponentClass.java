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

import java.awt.Color;
import java.io.Serializable;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.SortableArrayList;
import com.samskivert.util.StringUtil;

/**
 * Denotes a class of components to which {@link CharacterComponent}
 * objects belong. Examples include "Hat", "Head", and "Feet". A component
 * class dictates a component's rendering priority so that components can
 * be rendered in an order that causes them to overlap properly.
 *
 * <p> Components support render priority overrides for particular
 * actions, orientations or combinations of actions and orientations. The
 * system is currently structured with the expectation that the overrides
 * will be relatively few (less than fifteen, say) for any given component
 * class. A system that relied on many overrides for its components would
 * want to implement a more scalable algorithm for determining which, if
 * any, override matches a particular action and orientation combination.
 */
public class ComponentClass implements Serializable
{
    /** Used to effect custom render orders for particular actions,
     * orientations, etc. */
    public static class PriorityOverride
        implements Comparable, Serializable
    {
        /** The overridden render priority value. */
        public int renderPriority;

        /** The action, if any, for which this override is appropriate. */
        public String action;

        /** The orientations, if any, for which this override is
         * appropriate. */
        public ArrayIntSet orients;

        /**
         * Determines whether this priority override matches the specified
         * action and orientation combination.
         */
        public boolean matches (String action, int orient)
        {
            return (((orients == null) || orients.contains(orient)) &&
                    ((this.action == null) || this.action.equals(action)));
        }

        // documentation inherited from interface
        public int compareTo (Object other)
        {
            // overrides with both an action and an orientation should
            // come first in the list
            PriorityOverride po = (PriorityOverride)other;
            int pri = priority(), opri = po.priority();
            if (pri == opri) {
                return hashCode() - po.hashCode();
            } else {
                return pri - opri;
            }
        }

        protected int priority ()
        {
            int priority = 0;
            if (action != null) {
                priority++;
            }
            if (orients != null) {
                priority++;
            }
            return priority;
        }

        /** Generates a string representation of this instance. */
        public String toString ()
        {
            return "[pri=" + renderPriority + ", action=" + action +
                ", orients=" + orients + "]";
        }

        /** Increase this value when object's serialized state is impacted
         * by a class change (modification of fields, inheritance). */
        private static final long serialVersionUID = 1;
    }

    /** The component class name. */
    public String name;

    /** The default render priority. */
    public int renderPriority;

    /** The color classes to use when recoloring components of this class. May
     * be null if a system does not use recolorable components. */
    public String[] colors;

    /** Indicates the class name of the shadow layer to which this component
     * class contributes a shadow. */
    public String shadow;

    /** 1.0 for a normal component, the alpha value of the pre-composited
     * shadow for the special "shadow" component class. */
    public float shadowAlpha = 1.0f;

    /**
     * Creates an uninitialized instance suitable for unserialization or
     * population during XML parsing.
     */
    public ComponentClass ()
    {
    }

    /**
     * Returns the render priority appropriate for the specified action
     * and orientation.
     */
    public int getRenderPriority (String action, int orientation)
    {
        // because we expect there to be relatively few priority
        // overrides, we simply search linearly through the list for the
        // closest match
        int ocount = (_overrides != null) ? _overrides.size() : 0;
        for (int ii = 0; ii < ocount; ii++) {
            PriorityOverride over = (PriorityOverride)_overrides.get(ii);
            // based on the way the overrides are sorted, the first match
            // is the most specific and the one we want
            if (over.matches(action, orientation)) {
                return over.renderPriority;
            }
        }

        return renderPriority;
    }

    /**
     * Adds the supplied render priority override record to this component
     * class.
     */
    public void addPriorityOverride (PriorityOverride override)
    {
        if (_overrides == null) {
            _overrides = new SortableArrayList();
        }
        _overrides.insertSorted(override);
    }

    /**
     * Returns true if this component class contributes a shadow to a
     * particular shadow layer. Note: this is different from <em>being</em> a
     * shadow layer which is determined by calling {@link #isShadow}.
     */
    public boolean isShadowed ()
    {
        return (shadow != null);
    }

    /**
     * Returns true if this component class is a shadow layer rather than a
     * normal component class.
     */
    public boolean isShadow ()
    {
        return (shadowAlpha != 1.0f);
    }

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
        StringBuffer buf = new StringBuffer("[");
        buf.append("name=").append(name);
        buf.append(", pri=").append(renderPriority);
        if (colors != null) {
            buf.append(", colors=").append(StringUtil.toString(colors));
        }
        if (shadowAlpha != 1.0f) {
            buf.append(", shadow=").append(shadowAlpha);
        } else if (shadow != null) {
            buf.append(", shadow=").append(shadow);
        }
        return buf.append("]").toString();
    }

    /** A list of render priority overrides. */
    protected SortableArrayList _overrides;

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 3;
}
