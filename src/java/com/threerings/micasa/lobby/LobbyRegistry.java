//
// $Id: LobbyRegistry.java,v 1.1 2001/10/04 00:29:07 mdb Exp $

package com.threerings.micasa.lobdy;

import java.util.Properties;

import com.samskivert.util.Config;
import com.samskivert.util.PropertiesUtil;

import com.threerings.micasa.Log;
import com.threerings.micasa.server.MiCasaServer;

/**
 * The lobby registry is the primary class that coordinates the lobby
 * services on the client. It sets up the necessary invocation services
 * and keeps track of the lobbies in operation on the server. Only one
 * lobby registry should be created on a server.
 *
 * <p> Presently, the lobby registry is configured with lobbies via the
 * server configuration. An example configuration follows:
 *
 * <pre>
 * lobby_ids = foolobby, barlobby, bazlobby
 *
 * foolobby.mgrclass = com.threerings.micasa.lobby.LobbyManager
 * foolobby.config1 = some config value
 * foolobby.config2 = some other config value
 *
 * barlobby.mgrclass = com.threerings.micasa.lobby.LobbyManager
 * ...
 * </pre>
 *
 * This information will be loaded from the MiCasa server configuration
 * which means that it should live in
 * <code>rsrc/config/micasa/server.properties</code> somwhere in the
 * classpath where it will override the default MiCasa server properties
 * file.
 */
public class LobbyRegistry
{
    /**
     * A simple class for keeping track of information for each lobby in
     * operation on the server.
     */
    public static class Lobby
    {
        /** The object id of the lobby place object. */
        public int placeOid;

        /** The human readable name of the lobby. */
        public String name;
    }

    /**
     * Initializes the registry. It will use the supplied configuration
     * instance to determine which lobbies to load, etc.
     */
    public void init (Config config)
    {
        _config = config;

        // create our lobby managers
        String[] lmgrs = config.getValue(LOBIDS_KEY, (String[])null);
        if (lmgrs == null || lmgrs.length == 0) {
            Log.warning("No lobbies specified in config file (via '" +
                        LOBIDS_KEY + "' parameter.");

        } else {
            for (int i = 0; i < lmgrs.length; i++) {
                loadLobby(lmgrs[i]);
            }
        }
    }

    /**
     * Extracts the properties for a lobby from the server config and
     * creates and initializes the lobby manager.
     */
    protected void loadLobby (String lobbyId)
    {
        try {
            // extract the properties for this lobby
            Properties props =
                _config.getProperties(MiCasaServer.CONFIG_KEY);
            props = PropertiesUtil.getSubProperties(props, lobbyId);

            // get the lobby manager class
            String lmgrClass = props.getProperty("mgrclass");
            if (lmgrClass == null) {
                throw new Exception("Missing 'mgrclass' definition in " +
                                    "lobby configuration.");
            }

            // instantiate the manager class and create the lobby
            LobbyManager lobmgr = (LobbyManager)
                MiCasaServer.plreg.createPlace(Class.forName(lmgrClass));

            // initialize the manager
            lobmgr.init(props);

        } catch (Exception e) {
            Log.warning("Unable to create lobby manager " +
                        "[lobbyId=" + lobbyId + ", error=" + e + "].");
        }
    }

    /**
     * Returns information about all lobbies matching the supplied pattern
     * string. Presently the pattern is interpreted as a simple prefix and
     * all lobbies matching that prefix will be returned.
     *
     * @param pattern the pattern to match or null if all lobbies should
     * be returned.
     */
    public Lobby[] getLobbies (String pattern)
    {
        return null;
    }

    protected Config _config;

    /** The configuration key for the lobby managers list. */
    protected static final String LOBIDS_KEY =
        MiCasaServer.CONFIG_KEY + ".lobby_ids";
}
