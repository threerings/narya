//
// $Id: LobbyManager.java,v 1.4 2001/10/11 04:13:33 mdb Exp $

package com.threerings.micasa.lobby;

import java.util.Properties;
import com.samskivert.util.StringUtil;

import com.threerings.crowd.chat.ChatService;
import com.threerings.crowd.chat.ChatMessageHandler;
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

        // register a chat message handler because we want to support
        // chatting
        MessageHandler handler = new ChatMessageHandler();
        registerMessageHandler(ChatService.SPEAK_REQUEST, handler);
    }

    /** The universal game identifier for the game matchmade by this
     * lobby. */
    protected String _gameIdent;

    /** The human readable name of this lobby. */
    protected String _name;

    /** A reference to the lobby registry. */
    protected LobbyRegistry _lobreg;
}
