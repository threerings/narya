//
// $Id: Cluster.java,v 1.1 2001/08/10 01:31:25 shaper Exp $

package com.threerings.miso.scene;

import java.util.ArrayList;

/**
 * A <code>Cluster</code> is a gathering of <code>Location</code> objects
 * that represent a logical grouping for the purposes of display and
 * interaction in a scene.
 *
 * <p> This class is currently just a wrapper around an
 * <code>ArrayList</code>, but the theory is that we may soon want
 * more useful functionality encapsulated herein, and the
 * <code>Cluster</code> nomenclature is useful in any case.
 */
public class Cluster
{
    /**
     * Construct a <code>Cluster</code> object.
     */
    public Cluster ()
    {
	_locations = new ArrayList();
    }

    /**
     * Add a location to the cluster.
     *
     * @param loc the location.
     */
    public void add (Location loc)
    {
	_locations.add(loc);
    }

    /**
     * Return whether the cluster contains the given location.
     *
     * @param loc the location.
     */
    public boolean contains (Location loc)
    {
	return _locations.contains(loc);
    }

    /**
     * Removes the location from the cluster if present, and returns true
     * if the location was present, false if not.
     *
     * @param loc the location.
     */
    public boolean remove (Location loc)
    {
	return _locations.remove(loc);
    }

    /**
     * Return the number of locations in the cluster.
     */
    public int size ()
    {
	return _locations.size();
    }

    /**
     * Return the list of locations that the cluster is made up of.
     */
    public ArrayList getLocations ()
    {
	return _locations;
    }

    /** The list of locations in this cluster. */
    protected ArrayList _locations;
}
