//
// $Id: LobbyConfig.java,v 1.2 2001/10/09 20:22:51 mdb Exp $

package com.threerings.micasa.lobby;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.util.Properties;
import com.samskivert.util.StringUtil;

import com.threerings.cocktail.party.data.PlaceConfig;

import com.threerings.parlor.data.GameConfig;

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
     * Instantiates and returns a game config instance using the game
     * config classname provided by the lobby configuration parameters.
     *
     * @exception Exception thrown if a problem occurs loading or
     * instantiating the class.
     */
    public GameConfig getGameConfig ()
        throws Exception
    {
        return (GameConfig)Class.forName(_gameConfigClass).newInstance();
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
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeUTF(_gameConfigClass);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _gameConfigClass = in.readUTF();
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", game_config=").append(_gameConfigClass);
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
