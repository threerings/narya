//
// $Id: ClientController.java,v 1.1 2001/12/19 09:32:02 shaper Exp $

package com.threerings.micasa.simulator.client;

import java.awt.event.ActionEvent;
import com.samskivert.swing.Controller;
import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.*;
import com.threerings.presents.client.*;

import com.threerings.crowd.client.*;
import com.threerings.crowd.data.*;

import com.threerings.parlor.game.GameConfig;

import com.threerings.micasa.Log;
import com.threerings.micasa.simulator.util.SimulatorContext;
import com.threerings.micasa.simulator.data.SimulatorInfo;

/**
 * Responsible for top-level control of the client user interface.
 */
public class ClientController
    extends Controller
    implements ClientObserver
{
    /**
     * Creates a new client controller. The controller will set everything
     * up in preparation for logging on.
     */
    public ClientController (SimulatorContext ctx, SimulatorFrame frame)
    {
        // we'll want to keep these around
        _ctx = ctx;
        _frame = frame;

        // we want to know about logon/logoff
        _ctx.getClient().addObserver(this);
    }

    // documentation inherited
    public boolean handleAction (ActionEvent action)
    {
	String cmd = action.getActionCommand();

        if (cmd.equals("logoff")) {
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

        // get a handle on the simulator info
        SimulatorInfo siminfo = _ctx.getSimulatorInfo();

        // create the game config object
        GameConfig config = null;
        try {
            config = (GameConfig)
                Class.forName(siminfo.gameConfigClass).newInstance();
        } catch (Exception e) {
            Log.warning("Failed to instantiate game config " +
                        "[class=" + siminfo.gameConfigClass + "].");
            return;
        }

        // send the game creation request
        SimulatorDirector.createGame(
            client, config, siminfo.simClass, siminfo.playerCount);

        // our work here is done, as the location manager will move us
        // into the game room straightaway
    }

    // documentation inherited
    public void clientFailedToLogon (Client client, Exception cause)
    {
        Log.info("Client failed to logon [client=" + client +
                 ", cause=" + cause + "].");
    }

    // documentation inherited
    public void clientConnectionFailed (Client client, Exception cause)
    {
        Log.info("Client connection failed [client=" + client +
                 ", cause=" + cause + "].");
    }

    // documentation inherited
    public boolean clientWillLogoff (Client client)
    {
        Log.info("Client will logoff [client=" + client + "].");
        return true;
    }

    // documentation inherited
    public void clientDidLogoff (Client client)
    {
        Log.info("Client did logoff [client=" + client + "].");
    }

    protected SimulatorContext _ctx;
    protected SimulatorFrame _frame;
    protected BodyObject _body;
}
