//
// $Id: Spot.java,v 1.1 2001/08/10 00:47:34 shaper Exp $

package com.threerings.miso.scene;

import java.util.ArrayList;

/**
 * A <code>Spot</code> is a gathering of <code>Location</code> objects
 * that represent a logical grouping for the purposes of display and
 * interaction in a scene.
 *
 * <p> This class is currently just a wrapper around an
 * <code>ArrayList</code>, but the theory is that we may soon want
 * more useful functionality encapsulated herein, and the
 * <code>Spot</code> nomenclature is useful in any case.
 */
public class Spot
{
    /**
     * Construct a <code>Spot</code> object.
     */
    public Spot ()
    {
	_locations = new ArrayList();
    }

    /**
     * Add a location to the spot.
     *
     * @param loc the location.
     */
    public void add (Location loc)
    {
	_locations.add(loc);
    }

    /**
     * Return whether the spot contains the given location.
     *
     * @param loc the location.
     */
    public boolean contains (Location loc)
    {
	return _locations.contains(loc);
    }

    /**
     * Removes the location from the spot if present, and returns true
     * if the location was present, false if not.
     *
     * @param loc the location.
     */
    public boolean remove (Location loc)
    {
	return _locations.remove(loc);
    }

    /**
     * Return the number of locations in the spot.
     */
    public int size ()
    {
	return _locations.size();
    }

    /**
     * Return the list of locations that the spot is made up of.
     */
    public ArrayList getLocations ()
    {
	return _locations;
    }

    /** The list of locations in this spot. */
    protected ArrayList _locations;
}
