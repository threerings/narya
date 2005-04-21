//
// $Id$

package com.threerings.jme.data;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.jme.client.JabberController;

/**
 * Defines the necessary bits for our chat room.
 */
public class JabberConfig extends PlaceConfig
{
    // documentation inherited
    public Class getControllerClass ()
    {
        return JabberController.class;
    }

    // documentation inherited
    public String getManagerClassName ()
    {
        // nothing special needed on the server side
        return "com.threerings.crowd.server.PlaceManager";
    }
}
