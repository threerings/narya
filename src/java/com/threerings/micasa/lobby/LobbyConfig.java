//
// $Id: LobbyConfig.java,v 1.6 2002/07/23 05:54:52 mdb Exp $

package com.threerings.micasa.lobby;

import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JLabel;

import java.util.Properties;
import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.parlor.game.GameConfig;
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

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();
        out.writeUTF(_gameConfigClass);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        _gameConfigClass = in.readUTF();
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
