//
// $Id: ZoneDirector.java,v 1.9 2002/08/14 19:07:58 mdb Exp $

package com.threerings.whirled.zone.client;

import java.util.ArrayList;

import com.samskivert.util.ResultListener;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;

import com.threerings.crowd.data.PlaceConfig;

import com.threerings.whirled.client.SceneDirector;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.util.WhirledContext;

import com.threerings.whirled.zone.Log;
import com.threerings.whirled.zone.data.ZoneSummary;

/**
 * The zone director augments the scene services with the notion of zones.
 * Zones are self-contained, connected groups of scenes. The normal scene
 * director services can be used to move from scene to scene, but moving
 * to a new zone requires a special move request which can be accomplished
 * via the zone director. The zone director also makes available the zone
 * summary which provides information on the zone which can be used to
 * generate an overview map or similar.
 */
public class ZoneDirector extends BasicDirector
    implements ZoneReceiver, ZoneService.ZoneMoveListener
{
    /**
     * Constructs a zone director with the supplied context, and delegate
     * scene director (which the zone director will coordinate with when
     * moving from scene to scene). A zone director is required on the
     * client side for systems that wish to use the zone services.
     */
    public ZoneDirector (WhirledContext ctx, SceneDirector scdir)
    {
        super(ctx);
        _ctx = ctx;
        _scdir = scdir;

        // register for zone notifications
        _ctx.getClient().getInvocationDirector().registerReceiver(
            new ZoneDecoder(this));
    }

    /**
     * Returns the summary for the zone currently occupied by the client
     * or null if the client does not currently occupy a zone (not a
     * normal situation).
     */
    public ZoneSummary getZoneSummary ()
    {
        return _summary;
    }

    /**
     * Adds a zone observer to the list. This observer will subsequently
     * be notified of effected and failed zone changes.
     */
    public void addZoneObserver (ZoneObserver observer)
    {
        _observers.add(observer);
    }

    /**
     * Removes a zone observer from the list.
     */
    public void removeZoneObserver (ZoneObserver observer)
    {
        _observers.remove(observer);
    }

    /**
     * Requests that this client move the specified scene in the specified
     * zone. A request will be made and when the response is received, the
     * location observers will be notified of success or failure.
     */
    public boolean moveTo (int zoneId, int sceneId)
    {
        return moveTo(zoneId, sceneId, null);
    }

    /**
     * Requests that this client move the specified scene in the specified
     * zone. A request will be made and when the response is received, the
     * location observers will be notified of success or failure.
     */
    public boolean moveTo (int zoneId, int sceneId, ResultListener rl)
    {
        // if the requested zone is the same as our current zone, we just
        // want a regular old moveTo request
        if (_summary != null && zoneId == _summary.zoneId) {
            return _scdir.moveTo(sceneId);
        }

        // otherwise, we make a zoned moveTo request; prepare to move to
        // this scene (sets up pending data)
        if (!_scdir.prepareMoveTo(sceneId, rl)) {
            return false;
        }

        // let our zone observers know that we're attempting to switch
        // zones
        notifyObservers(new Integer(zoneId));

        // check the version of our cached copy of the scene to which
        // we're requesting to move; if we were unable to load it, assume
        // a cached version of zero
        int sceneVers = 0;
        SceneModel pendingModel = _scdir.getPendingModel();
        if (pendingModel != null) {
            sceneVers = pendingModel.version;
        }

        // issue a moveTo request
        _zservice.moveTo(_ctx.getClient(), zoneId, sceneId, sceneVers, this);
        return true;
    }

    // documentation inherited
    protected void fetchServices (Client client)
    {
        _zservice = (ZoneService)client.requireService(ZoneService.class);
    }

    /**
     * Called in response to a successful {@link ZoneService#moveTo}
     * request.
     */
    public void moveSucceeded (
        int placeId, PlaceConfig config, ZoneSummary summary)
    {
        // keep track of the summary
        _summary = summary;

        // pass the rest off to the standard scene transition code
        _scdir.moveSucceeded(placeId, config);

        // and let the zone observers know what's up
        notifyObservers(summary);
    }

    /**
     * Called in response to a successful {@link ZoneService#moveTo}
     * request when our cached scene was out of date and the server
     * determined that we needed an updated copy.
     */
    public void moveSucceededPlusUpdate (
        int placeId, PlaceConfig config, ZoneSummary summary, SceneModel model)
    {
        // keep track of the summary
        _summary = summary;

        // pass the rest off to the standard scene transition code
        _scdir.moveSucceededPlusUpdate(placeId, config, model);

        // and let the zone observers know what's up
        notifyObservers(summary);
    }

    /**
     * Called in response to a failed zoned <code>moveTo</code> request.
     */
    public void requestFailed (String reason)
    {
        // let the scene director cope
        _scdir.requestFailed(reason);

        // and let the observers know what's up
        notifyObservers(reason);
    }

    // documentation inherited from interface
    public void forcedMove (int zoneId, int sceneId)
    {
        Log.info("Moving at request of server [zoneId=" + zoneId +
                 ", sceneId=" + sceneId + "].");

        // clear out our old scene and place data
        _scdir.didLeaveScene();

        // move to the new zone and scene
        moveTo(zoneId, sceneId, null);
    }

    /**
     * Notifies observers of success or failure, depending on the type of
     * object provided as data.
     */
    protected void notifyObservers (Object data)
    {
        // let our observers know that all is well on the western front
        for (int i = 0; i < _observers.size(); i++) {
            ZoneObserver obs = (ZoneObserver)_observers.get(i);
            try {
                if (data instanceof Integer) {
                    obs.zoneWillChange(((Integer)data).intValue());
                } else if (data instanceof ZoneSummary) {
                    obs.zoneDidChange((ZoneSummary)data);
                } else {
                    obs.zoneChangeFailed((String)data);
                }

            } catch (Throwable t) {
                Log.warning("Zone observer choked during notification " +
                            "[data=" + data + ", obs=" + obs + "].");
                Log.logStackTrace(t);
            }
        }
    }

    /** A reference to the active client context. */
    protected WhirledContext _ctx;

    /** A reference to the scene director with which we coordinate. */
    protected SceneDirector _scdir;

    /** Provides access to zone services. */
    protected ZoneService _zservice;

    /** A reference to the zone summary for the currently occupied
     * zone. */
    protected ZoneSummary _summary;

    /** Our zone observer list. */
    protected ArrayList _observers = new ArrayList();
}
