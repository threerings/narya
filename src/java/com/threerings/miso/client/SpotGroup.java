//
// $Id: SpotGroup.java,v 1.1 2001/08/10 00:47:34 shaper Exp $

package com.threerings.miso.scene;

import java.util.ArrayList;

import com.threerings.miso.Log;

/**
 * The <code>SpotGroup</code> class manages all of the
 * <code>Spot</code> objects associated with a scene.  The scene should
 * only interact with spots via the <code>SpotGroup</code> class.
 */
public class SpotGroup
{
    /**
     * Construct a <code>SpotGroup</code> object.
     */
    public SpotGroup ()
    {
	_spots = new ArrayList();
    }

    /**
     * Construct a <code>SpotGroup</code> object, initializing it to
     * contain the given list of <code>Spot</code> objects.
     *
     * @param list the list of spots.
     */
    public SpotGroup (ArrayList spots)
    {
	_spots = spots;
    }

    /**
     * Return the list of <code>Spot</code> objects contained in the group.
     */
    public ArrayList getSpots ()
    {
	return _spots;
    }

    /**
     * Remove the given location from its spot, if any.
     *
     * @param loc the location.
     */
    public void remove (Location loc)
    {
	int size = _spots.size();
	for (int ii = 0; ii < size; ii++) {
	    Spot spot = (Spot)_spots.get(ii);

	    if (spot.contains(loc)) {
		spot.remove(loc);

		// remove the spot itself if it contains no more locations
		if (spot.size() == 0) {
		    _spots.remove(spot);
		}

		// we know the location can only reside in at most one spot
		break;
	    }
	}
    }

    /**
     * Re-spot the given location to be placed within the given spot
     * index.
     *
     * <p> If the spot index is -1, the location is simply removed
     * from any spot it may reside in.  Otherwise, the location is
     * removed from any location it may already be in, and placed in
     * the spot corresponding to the requested spot index.
     *
     * <p> The spot index may be equal to the current number of spots
     * in the group, in which case a new spot object will be created
     * that initially contains only the given location.
     *
     * @param loc the location.
     * @param spotidx the spot index, or -1 to remove the location
     *                from any containing spot.
     */
    public void respot (Location loc, int spotidx)
    {
	// just remove the location if spotidx is -1
	if (spotidx == -1) {
	    remove(loc);
	    return;
	}

	// make sure we're okay with the requested spot index
	int size = _spots.size();
	if (spotidx > size) {
	    Log.warning("Attempt to respot location to a non-contiguous " +
			"spot index [loc=" + loc + ", spotidx=" +
			spotidx + "].");
	    return;
	}

	// get the spot object the location's to be placed in
	Spot spot = null;
	if (spotidx == size) {
	    // the location's being added to a new spot, so create it
	    _spots.add(spot = new Spot());

	} else {
	    // retrieve the spot we're planning to place the location in
	    spot = (Spot)_spots.get(spotidx);

	    // this should never happen, but sanity-check anyway
	    if (spot == null) {
		Log.warning("Failed to retrieve spot [spotidx=" +
			    spotidx + "].");
		return;
	    }

	    // bail if the spot already contains the location
	    if (spot.contains(loc)) return;
	}

	// remove the location from any other spot it may already be in
	remove(loc);

	// add the location to the spot
	spot.add(loc);
    }

    /**
     * Return the number of spots in the group.
     */
    public int size ()
    {
	return _spots.size();
    }

    /** The list of spots in the spot group. */
    protected ArrayList _spots;
}
