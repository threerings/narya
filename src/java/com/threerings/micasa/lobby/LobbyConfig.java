//
// $Id$
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

import javax.swing.JComponent;
import javax.swing.JLabel;

import java.util.Properties;
import com.samskivert.util.StringUtil;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.micasa.util.MiCasaContext;

public class LobbyConfig extends PlaceConfig
{
    // documentation inherited
    public Class getControllerClass ()
    {
        return LobbyController.class;
    }

    // documentation inherited
    public String getManagerClassName ()
    {
        return "com.threerings.micasa.lobby.LobbyManager";
    }

    /**
     * Derived classes override this function and create the appropriate
     * matchmaking user interface component.
     */
    public JComponent createMatchMakingView (MiCasaContext ctx)
    {
        return new JLabel("Match-making view goes here.");
    }

    /**
     * Instantiates and returns a game config instance using the game
     * config classname provided by the lobby configuration parameters.
     *
     * @exception Exception thrown if a problem occurs loading or
     * instantiating the class.
     */
    public GameConfig getGameConfig ()
        throws Exception
    {
        return (GameConfig)_loader.loadClass(_gameConfigClass).newInstance();
    }

    /**
     * Initializes this lobby config object with the properties that are
     * used to configure the lobby. This is called on the server when the
     * lobby is loaded.
     */
    public void init (Properties config)
        throws Exception
    {
        _gameConfigClass = getConfigValue(config, "game_config");
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        if (buf.length() > 1) {
            buf.append(", ");
        }
        buf.append("game_config=").append(_gameConfigClass);
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

    /** The name of the game config class that represents the type of game
     * we are matchmaking for in this lobby. */
    protected String _gameConfigClass;
}
