//
// $Id: TrackedObject.java,v 1.6 2004/08/28 01:23:31 ray Exp $
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

package com.threerings.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.samskivert.util.Tuple;

/**
 * Used to perform rudimentary memory profiling on large, running systems
 * where it is impractical to operate a real profiler. Instances of {@link
 * TrackedObject} record their existence when constructed and their
 * destruction when finalized which, combined with periodic queries of the
 * current total object count (by class) logged and graphed (gnuplot is
 * your friend), can provide an indication of systems that are improperly
 * retaining references.
 */
public class TrackedObject
{
    // instance initializer - called whenever an instance is constructed
    {
        incrementInstanceCount();
    }

    // documentation inherited
    protected Object clone ()
        throws CloneNotSupportedException
    {
        Object o = super.clone();
        incrementInstanceCount();
        return o;
    }

    /** Records that this object came into existence. */
    protected final void incrementInstanceCount ()
    {
        Class clazz = getClass();
        synchronized (_map) {
            int[] count = (int[])_map.get(clazz);
            if (count == null) {
                _map.put(clazz, count = new int[1]);
            }
            count[0]++;
        }
    }

    /** Records that this object was collected. */
    protected void finalize ()
        throws Throwable
    {
        Class clazz = getClass();
        synchronized (_map) {
            int[] count = (int[])_map.get(clazz);
            if (count != null) {
                count[0]--;
            } else {
                Log.warning("Finalized TrackedObject missing counter! " +
                            "[class=" + clazz + "].");
            }
        }

        super.finalize();
    }

    /**
     * Returns a {@link Tuple} containing an array of {@link Class}
     * instances and an <code>int[]</code> array that represent the number
     * of outstanding instance of all {@link TrackedObject}s in the VM.
     */
    public static Tuple getSnapshot ()
    {
        Class[] classes = null;
        int[] counts = null;
        synchronized (_map) {
            classes = new Class[_map.size()];
            counts = new int[_map.size()];
            Iterator iter = _map.entrySet().iterator();
            for (int ii = 0; iter.hasNext(); ii++) {
                Map.Entry entry = (Map.Entry)iter.next();
                classes[ii] = (Class)entry.getKey();
                counts[ii] = ((int[])entry.getValue())[0];
            }
        }
        return new Tuple(classes, counts);
    }

    /** Tracks a mapping from {@link Class} object to active count. */
    protected static HashMap _map = new HashMap();
}
