//
// $Id: Cluster.java,v 1.3 2001/09/28 01:24:54 mdb Exp $

package com.threerings.miso.scene;

import java.util.List;
import java.util.ArrayList;

import com.samskivert.util.StringUtil;

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
    public List getLocations ()
    {
	return _locations;
    }

    /**
     * Return a string representation of this object.
     */
    public String toString ()
    {
	return StringUtil.toString(_locations);
    }

    /** The list of locations in this cluster. */
    protected ArrayList _locations;
}
