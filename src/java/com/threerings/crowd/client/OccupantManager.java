//
// $Id: OccupantManager.java,v 1.1 2001/08/20 20:54:56 mdb Exp $

package com.threerings.cocktail.party.client;

import java.util.ArrayList;

import com.threerings.cocktail.cher.dobj.*;
import com.threerings.cocktail.cher.util.IntMap;

import com.threerings.cocktail.party.Log;
import com.threerings.cocktail.party.data.OccupantInfo;
import com.threerings.cocktail.party.data.PlaceObject;
import com.threerings.cocktail.party.util.PartyContext;

/**
 * The occupant manager listens for occupants of places to enter and exit,
 * and dispatches notices to interested parties about these events.
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
public class OccupantManager
    implements LocationObserver, Subscriber
{
    /**
     * Constructs a new occupant manager with the supplied context.
     */
    public OccupantManager (PartyContext ctx)
    {
        // register ourselves as a location observer
        ctx.getLocationManager().addLocationObserver(this);
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

    // inherit documentation
    public boolean locationMayChange (int placeId)
    {
        // we've got no opinion
        return true;
    }

    // inherit documentation
    public void locationDidChange (PlaceObject place)
    {
        // unsubscribe from the old place object if there was one
        if (_place != null) {
            _place.removeSubscriber(this);
        }

        // subscribe to the new one
        _place = place;
        _place.addSubscriber(this);
    }

    // inherit documentation
    public void locationChangeFailed (int placeId, String reason)
    {
        // nothing to do here either
    }

    // inherit documentation
    public void objectAvailable (DObject object)
    {
        // nothing doing
    }

    // inherit documentation
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        // nothing doing
    }

    // inherit documentation
    public boolean handleEvent (DEvent event, DObject target)
    {
        // we care about occupant added and removed events only
        if (event instanceof ObjectAddedEvent) {
            ObjectAddedEvent oe = (ObjectAddedEvent)event;
            if (oe.getName().equals(PlaceObject.OCCUPANTS)) {
                handleOccupantAdded(oe.getOid());
            }

        } else if (event instanceof ObjectRemovedEvent) {
            ObjectRemovedEvent oe = (ObjectRemovedEvent)event;
            if (oe.getName().equals(PlaceObject.OCCUPANTS)) {
                handleOccupantRemoved(oe.getOid());
            }
        }

        return true;
    }

    /**
     * Deals with all of the processing when an occupant shows up.
     */
    protected void handleOccupantAdded (int bodyOid)
    {
        Object key = new Integer(bodyOid);

        // get the occupant info from the place object
        OccupantInfo info = (OccupantInfo)_place.occupantInfo.get(key);
        if (info == null) {
            Log.warning("Occupant entered but we have no info for them! " +
                        "[boid=" + bodyOid + ", place=" + _place + "].");
            return;
        }

        // put the info in our cache for use when we get a left event
        _ocache.put(bodyOid, info);

        // now let the occupant observers know what's up
        for (int i = 0; i < _observers.size(); i++) {
            OccupantObserver obs = (OccupantObserver)_observers.get(i);
            obs.occupantEntered(info);
        }
    }

    /**
     * Deals with all of the processing when an occupant leaves.
     */
    protected void handleOccupantRemoved (int bodyOid)
    {
        // see if we have an occupant object for this body
        OccupantInfo info = (OccupantInfo)_ocache.get(bodyOid);
        if (info == null) {
            Log.warning("Occupant removed but no cached info for them? " +
                        "[boid=" + bodyOid + ", place=" + _place + "].");
            return;
        }

        // let the occupant observers know what's up
        for (int i = 0; i < _observers.size(); i++) {
            OccupantObserver obs = (OccupantObserver)_observers.get(i);
            obs.occupantEntered(info);
        }
    }

    protected ArrayList _observers = new ArrayList();
    protected PlaceObject _place;
    protected IntMap _ocache = new IntMap();
}
