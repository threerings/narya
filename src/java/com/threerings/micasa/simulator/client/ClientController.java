//
// $Id: ClientController.java,v 1.9 2002/11/12 01:42:28 shaper Exp $

package com.threerings.micasa.simulator.client;

import java.awt.event.ActionEvent;
import com.samskivert.swing.Controller;
import com.samskivert.util.StringUtil;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.SessionObserver;

import com.threerings.crowd.data.BodyObject;

import com.threerings.parlor.game.GameConfig;
import com.threerings.parlor.util.ParlorContext;

import com.threerings.micasa.Log;
import com.threerings.micasa.simulator.data.SimulatorInfo;

/**
 * Responsible for top-level control of the client user interface.
 */
public class ClientController extends Controller
    implements SessionObserver
{
    /** Command constant used to logoff the client. */
    public static final String LOGOFF = "logoff";

    /**
     * Creates a new client controller. The controller will set everything
     * up in preparation for logging on.
     */
    public ClientController (ParlorContext ctx, SimulatorFrame frame,
                             SimulatorInfo info)
    {
        // we'll want to keep these around
        _ctx = ctx;
        _frame = frame;
        _info = info;

        // we want to know about logon/logoff
        _ctx.getClient().addClientObserver(this);
    }

    // documentation inherited
    public boolean handleAction (ActionEvent action)
    {
	String cmd = action.getActionCommand();

        if (cmd.equals(LOGOFF)) {
            // request that we logoff
            _ctx.getClient().logoff(true);
            return true;
        }

        Log.info("Unhandled action: " + action);
        return false;
    }

    // documentation inherited
    public void clientDidLogon (Client client)
    {
        Log.info("Client did logon [client=" + client + "].");

        // keep the body object around for stuff
        _body = (BodyObject)client.getClientObject();

        // have at it
        createGame(client);
    }

    public void createGame (Client client)
    {
        GameConfig config = null;
        try {
            // create the game config object
            config = (GameConfig)
                Class.forName(_info.gameConfigClass).newInstance();

            // get the simulator service and use it to request that our
            // game be created
            SimulatorService sservice = (SimulatorService)
                client.requireService(SimulatorService.class);
            sservice.createGame(
                client, config, _info.simClass, _info.playerCount);

            // our work here is done, as the location manager will move us
            // into the game room straightaway

        } catch (Exception e) {
            Log.warning("Failed to instantiate game config " +
                        "[class=" + _info.gameConfigClass +
                        ", error=" + e + "].");
        }
    }

    // documentation inherited
    public void clientObjectDidChange (Client client)
    {
        // regrab our body object
        _body = (BodyObject)client.getClientObject();
    }

    // documentation inherited
    public void clientDidLogoff (Client client)
    {
        Log.info("Client did logoff [client=" + client + "].");
    }

    protected ParlorContext _ctx;
    protected SimulatorFrame _frame;
    protected SimulatorInfo _info;
    protected BodyObject _body;
}
