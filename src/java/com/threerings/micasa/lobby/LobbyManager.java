//
// $Id: LobbyManager.java,v 1.8 2004/08/27 02:12:50 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.micasa.lobby;

import java.util.Properties;
import com.samskivert.util.StringUtil;

import com.threerings.crowd.server.PlaceManager;
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
    public void init (LobbyRegistry lobreg, Properties config)
        throws Exception
    {
        // look up some configuration parameters
        _gameIdent = getConfigValue(config, "ugi");
        _name = getConfigValue(config, "name");

        // keep this for later
        _lobreg = lobreg;

        Log.info("Lobby manager initialized [ident=" + _gameIdent +
                 ", name=" + _name + "].");
    }

    /** Looks up a configuration property in the supplied properties
     * object and throws an exception if it's not found. */
    protected String getConfigValue (Properties config, String key)
        throws Exception
    {
        String value = config.getProperty(key);
        if (StringUtil.blank(value)) {
            throw new Exception("Missing '" + key + "' definition in " +
                                "lobby configuration.");
        }
        return value;
    }

    // documentation inherited
    protected Class getPlaceObjectClass ()
    {
        return LobbyObject.class;
    }

    // documentation inherited
    protected void didStartup ()
    {
        super.didStartup();

        // let the lobby registry know that we're up and running
        _lobreg.lobbyReady(_plobj.getOid(), _gameIdent, _name);
    }

    /** The universal game identifier for the game matchmade by this
     * lobby. */
    protected String _gameIdent;

    /** The human readable name of this lobby. */
    protected String _name;

    /** A reference to the lobby registry. */
    protected LobbyRegistry _lobreg;
}
