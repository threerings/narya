//
// $Id: ZoneRegistry.java,v 1.2 2001/12/04 01:06:33 mdb Exp $

package com.threerings.whirled.zone.server;

import com.samskivert.util.Config;
import com.samskivert.util.HashIntMap;

import com.threerings.presents.server.InvocationManager;

import com.threerings.whirled.Log;
import com.threerings.whirled.server.SceneRegistry;

/**
 * The zone registry takes care of mapping zone requests to the
 * appropriate registered zone manager.
 */
public class ZoneRegistry
{
    /**
     * Creates a zone manager with the supplied configuration.
     */
    public ZoneRegistry (
        Config config, InvocationManager invmgr, SceneRegistry screg)
    {
        // create a zone provider and register it with the invocation
        // services
        ZoneProvider provider = new ZoneProvider(this, screg);
        invmgr.registerProvider(ZoneProvider.MODULE_NAME, provider);
    }

    /**
     * Registers the supplied zone manager as the manager for the
     * specified zone type. Zone types are 7 bits and managers are
     * responsible for making sure they don't use a zone type that
     * collides with another manager (given that we have only three zone
     * types at present, this doesn't seem unreasonable).
     */
    public void registerZoneManager (byte zoneType, ZoneManager manager)
    {
        ZoneManager old = (ZoneManager)_managers.get(zoneType);
        if (old != null) {
            Log.warning("Zone manager already registered with requested " +
                        "type [type=" + zoneType + ", old=" + old +
                        ", new=" + manager + "].");
        } else {
            _managers.put(zoneType, manager);
        }
    }

    /**
     * Returns the zone manager that handles the specified zone id.
     */
    public ZoneManager getZoneManager (int zoneId)
    {
        int zoneType = (0xFF000000 & zoneId) >> 24;
        return (ZoneManager)_managers.get(zoneType);
    }

    /** A table of zone managers. */
    protected HashIntMap _managers = new HashIntMap();
}
