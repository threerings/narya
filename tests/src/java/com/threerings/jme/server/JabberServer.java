//
// $Id$

package com.threerings.jme.server;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.jme.data.JabberConfig;

/**
 * A basic server that creates a single room and sticks everyone in it
 * where they can chat with one another.
 */
public class JabberServer extends CrowdServer
{
    // documentation inherited
    public void init ()
        throws Exception
    {
        super.init();

        // create a single location
        plreg.createPlace(
            new JabberConfig(), new PlaceRegistry.CreationObserver() {
            public void placeCreated (PlaceObject place, PlaceManager pmgr) {
                Log.info("Created chat room " + pmgr.where() + ".");
                _place = pmgr;
            }
        });
    }

    public static void main (String[] args)
    {
        JabberServer server = new JabberServer();
        try {
            server.init();
            server.run();
        } catch (Exception e) {
            Log.warning("Unable to initialize server.");
            Log.logStackTrace(e);
        }
    }

    protected PlaceManager _place;
}
