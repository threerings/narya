//
// $Id: TrackedObject.java,v 1.1 2004/08/13 23:44:16 mdb Exp $

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
    /** Records that this object came into existence. */
    public TrackedObject ()
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
