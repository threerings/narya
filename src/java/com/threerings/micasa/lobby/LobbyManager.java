//
// $Id: LobbyManager.java,v 1.1 2001/10/04 00:29:07 mdb Exp $

package com.threerings.micasa.lobdy;

import java.util.Properties;

import com.threerings.cocktail.party.server.PlaceManager;
import com.threerings.micasa.Log;

/**
 * Takes care of the server side of a particular lobby.
 */
public class LobbyManager extends PlaceManager
{
    /**
     * Initializes this lobby manager with its configuration properties.
     *
     * @exception Exception thrown if a configuration error is detected.
     */
    public void init (Properties config)
        throws Exception
    {
        Log.info("Lobby manager initialized.");
    }

    protected Class getPlaceObjectClass ()
    {
        return LobbyObject.class;
    }
}
