//
// $Id$

package com.threerings.crowd.data;

import com.threerings.crowd.client.JabberController;
import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.data.PlaceConfig;

/**
 * Defines the necessary bits for our chat room.
 */
public class JabberConfig extends PlaceConfig
{
    // documentation inherited
    public PlaceController createController ()
    {
        return new JabberController();
    }

    // documentation inherited
    public String getManagerClassName ()
    {
        // nothing special needed on the server side
        return "com.threerings.crowd.server.PlaceManager";
    }
}
