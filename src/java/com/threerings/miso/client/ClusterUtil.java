//
// $Id: ClusterUtil.java,v 1.2 2001/08/10 21:17:07 shaper Exp $

package com.threerings.miso.scene;

import java.util.ArrayList;

import com.threerings.miso.Log;

/**
 * The <code>ClusterUtil</code> class provides utility routines for
 * working with a list of <code>Cluster</code> objects associated with
 * a scene.
 */
public class ClusterUtil
{
    /**
     * Return the cluster index number the given location is in, or -1
     * if the location is not in any cluster.
     *
     * @param clusters the cluster list.
     * @param loc the location.
     */
    public static int getClusterIndex (ArrayList clusters, Location loc)
    {
	int size = clusters.size();
	for (int ii = 0; ii < size; ii++) {
	    Cluster cluster = (Cluster)clusters.get(ii);
	    if (cluster.contains(loc)) return ii;
	}
	return -1;
    }

    /**
     * Remove the given location from its cluster, if any.
     *
     * @param clusters the cluster list.
     * @param loc the location.
     */
    public static void remove (ArrayList clusters, Location loc)
    {
	int size = clusters.size();
	for (int ii = 0; ii < size; ii++) {
	    Cluster cluster = (Cluster)clusters.get(ii);

	    if (cluster.contains(loc)) {
		cluster.remove(loc);

		// remove the cluster itself if it contains no more locations
		if (cluster.size() == 0) {
		    clusters.remove(cluster);
		}

		// we know the location can only reside in at most one cluster
		break;
	    }
	}
    }

    /**
     * Re-group the given location to be placed within the given cluster
     * index.
     *
     * <p> If the cluster index is -1, the location is simply removed
     * from any cluster it may reside in.  Otherwise, the location is
     * removed from any location it may already be in, and placed in
     * the cluster corresponding to the requested cluster index.
     *
     * <p> The cluster index may be equal to the current number of clusters
     * in the group, in which case a new cluster object will be created
     * that initially contains only the given location.
     *
     * @param clusters the cluster list.
     * @param loc the location.
     * @param clusteridx the cluster index, or -1 to remove the location
     *                   from any cluster.
     */
    public static void regroup (ArrayList clusters, Location loc,
				int clusteridx)
    {
	// just remove the location if clusteridx is -1
	if (clusteridx == -1) {
	    remove(clusters, loc);
	    return;
	}

	// make sure we're okay with the requested cluster index
	int size = clusters.size();
	if (clusteridx > size) {
	    Log.warning("Attempt to regroup location to a non-contiguous " +
			"cluster index [loc=" + loc + ", clusteridx=" +
			clusteridx + "].");
	    return;
	}

	// get the cluster object the location's to be placed in
	Cluster cluster = null;
	if (clusteridx == size) {
	    // the location's being added to a new cluster, so create it
	    clusters.add(cluster = new Cluster());

	} else {
	    // retrieve the cluster we're planning to place the location in
	    cluster = (Cluster)clusters.get(clusteridx);

	    // this should never happen, but sanity-check anyway
	    if (cluster == null) {
		Log.warning("Failed to retrieve cluster [clusteridx=" +
			    clusteridx + "].");
		return;
	    }

	    // bail if the cluster already contains the location
	    if (cluster.contains(loc)) return;
	}

	// remove the location from any other cluster it may already be in
	remove(clusters, loc);

	// add the location to the cluster
	cluster.add(loc);
    }
}
