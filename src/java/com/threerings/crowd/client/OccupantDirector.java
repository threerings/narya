//
// $Id: OccupantDirector.java,v 1.6 2002/10/27 23:30:56 shaper Exp $

package com.threerings.crowd.client;

import java.util.Iterator;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.ObserverList;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

/**
 * The occupant director listens for occupants of places to enter and
 * exit, and dispatches notices to interested parties about these events.
 *
 * <p> It will eventually provide a framework for keeping track of
 * occupant information in a network efficient manner. The idea being that
 * we want to store as little information about occupants as possible in
 * the place object (probably just body oid and username), but upon
 * entering a place, this will be all we know about the occupants. We then
 * dispatch a request to get information about all of the occupants in the
 * room (things like avatar information for a graphical display or perhaps
 * their ratings in the game that is associated with a place for a gaming
 * site) which we then pass on to the occupant observers when it becomes
 * available.
 *
 * <p> This information would be cached and we could return cached
 * information for occupants for which we have cached info. We will
 * probably want to still make a request for the occupant info so that we
 * can update non-static occupant data rather than permanently using
 * what's in the cache.
 */
public class OccupantDirector extends BasicDirector
    implements LocationObserver, SetListener
{
    /**
     * Constructs a new occupant director with the supplied context.
     */
    public OccupantDirector (CrowdContext ctx)
    {
        super(ctx);

        // register ourselves as a location observer
        ctx.getLocationDirector().addLocationObserver(this);
    }

    /**
     * Adds the specified occupant observer to the list.
     */
    public void addOccupantObserver (OccupantObserver obs)
    {
        _observers.add(obs);
    }

    /**
     * Removes the specified occupant observer from the list.
     */
    public void removeOccupantObserver (OccupantObserver obs)
    {
        _observers.remove(obs);
    }

    /**
     * Returns the occupant info for the user in question if it exists in
     * the currently occupied place. Returns null if no occupant info
     * exists for the specified body.
     */
    public OccupantInfo getOccupantInfo (int bodyOid)
    {
        // make sure we're somewhere
        return (_place == null) ? null :
            (OccupantInfo)_place.occupantInfo.get(new Integer(bodyOid));
    }

    /**
     * Returns the occupant info for the user in question if it exists in
     * the currently occupied place. Returns null if no occupant info
     * exists with the specified username.
     */
    public OccupantInfo getOccupantInfo (String username)
    {
        // make sure we're somewhere
        if (_place == null) {
            return null;
        }

        Iterator iter = _place.occupantInfo.entries();
        while (iter.hasNext()) {
            OccupantInfo info = (OccupantInfo)iter.next();
            if (info.username.equals(username)) {
                return info;
            }
        }
        return null;
    }

    // documentation inherited from interface
    public void clientDidLogoff (Client client)
    {
        // clear things out
        if (_place != null) {
            _place.removeListener(this);
            _place = null;
        }
        _ocache.clear();
    }

    // inherit documentation
    public boolean locationMayChange (int placeId)
    {
        // we've got no opinion
        return true;
    }

    // inherit documentation
    public void locationDidChange (PlaceObject place)
    {
        // unlisten to the old place object if there was one
        if (_place != null) {
            _place.removeListener(this);
            // clear out the occupant cache for the previous location
            _ocache.clear();
        }

        // listen to the new one
        _place = place;
        if (_place != null) {
            _place.addListener(this);

            // cache the occupant info for the occupants in this room
            Iterator iter = _place.occupantInfo.entries();
            while (iter.hasNext()) {
                OccupantInfo info = (OccupantInfo)iter.next();
                _ocache.put(info.getBodyOid(), info);
            }
        }
    }

    // inherit documentation
    public void locationChangeFailed (int placeId, String reason)
    {
        // nothing to do here either
    }

    /**
     * Deals with all of the processing when an occupant shows up.
     */
    public void entryAdded (EntryAddedEvent event)
    {
        // bail if this isn't for the OCCUPANT_INFO field
        if (!event.getName().equals(PlaceObject.OCCUPANT_INFO)) {
            return;
        }

        // put the info in our cache for use when we get a left event
        final OccupantInfo info = (OccupantInfo)event.getEntry();
        int bodyOid = info.getBodyOid();
        _ocache.put(bodyOid, info);

        // now let the occupant observers know what's up
        _observers.apply(new ObserverList.ObserverOp() {
            public boolean apply (Object observer) {
                ((OccupantObserver)observer).occupantEntered(info);
                return true;
            }
        });
    }

    /**
     * Deals with all of the processing when an occupant is updated.
     */
    public void entryUpdated (EntryUpdatedEvent event)
    {
        // bail if this isn't for the OCCUPANT_INFO field
        if (!event.getName().equals(PlaceObject.OCCUPANT_INFO)) {
            return;
        }

        final OccupantInfo info = (OccupantInfo)event.getEntry();
        int bodyOid = info.getBodyOid();

        // grab the old info to give observers a chance to figure out what
        // changed
        final OccupantInfo oinfo = (OccupantInfo)_ocache.get(bodyOid);
        if (oinfo == null) {
            Log.warning("Urk! Occupant updated for whom we we have no " +
                        "prior info record [info=" + info + "].");
        }

        // update our cache
        _ocache.put(bodyOid, info);

        // now let the occupant observers know what's up
        _observers.apply(new ObserverList.ObserverOp() {
            public boolean apply (Object observer) {
                ((OccupantObserver)observer).occupantUpdated(oinfo, info);
                return true;
            }
        });
    }

    /**
     * Deals with all of the processing when an occupant leaves.
     */
    public void entryRemoved (EntryRemovedEvent event)
    {
        // bail if this isn't for the OCCUPANT_INFO field
        if (!event.getName().equals(PlaceObject.OCCUPANT_INFO)) {
            return;
        }

        int bodyOid = ((Integer)event.getKey()).intValue();
        // see if we have an occupant object for this body
        final OccupantInfo info = (OccupantInfo)_ocache.get(bodyOid);
        if (info == null) {
            Log.warning("Occupant removed but no cached info for them? " +
                        "[boid=" + bodyOid + ", place=" + _place + "].");
            return;
        }

        // let the occupant observers know what's up
        _observers.apply(new ObserverList.ObserverOp() {
            public boolean apply (Object observer) {
                ((OccupantObserver)observer).occupantLeft(info);
                return true;
            }
        });
    }

    /** The occupant observers to keep abreast of occupant antics. */
    protected ObserverList _observers =
        new ObserverList(ObserverList.SAFE_IN_ORDER_NOTIFY);

    /** The user's current location. */
    protected PlaceObject _place;

    /** A cache of the occupant info for all users in our current
     * location. */
    protected HashIntMap _ocache = new HashIntMap();
}
