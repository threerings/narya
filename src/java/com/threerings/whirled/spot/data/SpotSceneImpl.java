//
// $Id: SpotSceneImpl.java,v 1.2 2003/06/11 04:14:11 mdb Exp $

package com.threerings.whirled.spot.data;

import java.util.Iterator;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.ListUtil;

import com.threerings.whirled.spot.Log;

/**
 * An implementation of the {@link SpotScene} interface.
 */
public class SpotSceneImpl
    implements SpotScene
{
    /**
     * Creates an instance that will obtain data from the supplied spot
     * scene model.
     */
    public SpotSceneImpl (SpotSceneModel smodel)
    {
        _smodel = smodel;
        readPortals();
    }

    protected void readPortals ()
    {
        _portals.clear();
        for (int ii = 0, ll = _smodel.portals.length; ii < ll; ii++) {
            Portal port = _smodel.portals[ii];
            _portals.put(port.portalId, port);
        }
    }

    /**
     * Instantiates a blank scene implementation.
     */
    public SpotSceneImpl ()
    {
        _smodel = new SpotSceneModel();
    }

    // documentation inherited from interface
    public Portal getPortal (int portalId)
    {
        return (Portal)_portals.get(portalId);
    }

    // documentation inherited from interface
    public int getPortalCount ()
    {
        return _portals.size();
    }

    // documentation inherited from interface
    public Iterator getPortals ()
    {
        return _portals.values().iterator();
    }

    // documentation inherited from interface
    public Portal getDefaultEntrance ()
    {
        return getPortal(_smodel.defaultEntranceId);
    }

    // documentation inherited from interface
    public void addPortal (Portal portal)
    {
        // compute a new portal id for our friend the portal
        portal.portalId = 0;
        for (short ii = 1; ii < MAX_PORTAL_ID; ii++) {
            if (!_portals.containsKey(ii)) {
                portal.portalId = ii;
                break;
            }
        }
        if (portal.portalId == 0) {
            Log.warning("Unable to assign id to new portal " +
                        "[scene=" + this + "].");
            return;
        }

        // add this beyotch to our model
        _smodel.addPortal(portal);

        // and slap it into our table
        _portals.put(portal.portalId, portal);
    }

    // documentation inherited from interface
    public void removePortal (Portal portal)
    {
        // remove the portal from our mapping
        _portals.remove(portal.portalId);

        // remove it from the model
        _smodel.removePortal(portal);
    }

    /**
     * Used when we're being parsed from an XML scene model.
     */
    public void setDefaultEntranceId (int defaultEntranceId)
    {
        _smodel.defaultEntranceId = defaultEntranceId;
    }

    // documentation inherited from interface
    public void setDefaultEntrance (Portal portal)
    {
        _smodel.defaultEntranceId = (portal == null) ? -1 : portal.portalId;
    }

    /**
     * This should be called if a scene update was received that caused
     * our underlying scene model to change.
     */
    public void updateReceived ()
    {
        readPortals();
    }

    /** A casted reference to our scene model. */
    protected SpotSceneModel _smodel;

    /** A mapping from portal id to portal. */
    protected HashIntMap _portals = new HashIntMap();

    /** We don't allow more than ~32k portals in a scene. Things would
     * slow down *way* before we got there. */
    protected static final int MAX_PORTAL_ID = Short.MAX_VALUE;
}
