//
// $Id: ClientController.java,v 1.14 2002/05/21 04:47:48 mdb Exp $

package com.threerings.micasa.client;

import java.awt.event.ActionEvent;
import com.samskivert.swing.Controller;
import com.samskivert.util.StringUtil;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.SessionObserver;

import com.threerings.crowd.data.BodyObject;

import com.threerings.micasa.Log;
import com.threerings.micasa.data.MiCasaBootstrapData;
import com.threerings.micasa.util.MiCasaContext;

/**
 * Responsible for top-level control of the client user interface.
 */
public class ClientController extends Controller
    implements SessionObserver
{
    /**
     * Creates a new client controller. The controller will set everything
     * up in preparation for logging on.
     */
    public ClientController (MiCasaContext ctx, MiCasaFrame frame)
    {
        // we'll want to keep these around
        _ctx = ctx;
        _frame = frame;

        // we want to know about logon/logoff
        _ctx.getClient().addClientObserver(this);

        // create the logon panel and display it
        _logonPanel = new LogonPanel(_ctx);
        _frame.setPanel(_logonPanel);
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

        // figure out where to go
        int moveOid = -1;

        // hacky hack
        String jumpOidStr = null;
        try {
            jumpOidStr = System.getProperty("jumpoid");
        } catch (SecurityException se) {
            Log.info("Not checking for jumpOid as we're in an applet.");
        }

        if (jumpOidStr != null) {
            try {
                moveOid = Integer.parseInt(jumpOidStr);
            } catch (NumberFormatException nfe) {
                Log.warning("Invalid jump oid [oid=" + jumpOidStr +
                            ", err=" + nfe + "].");
            }

        } else if (_body.location != -1) {
            // if we were already in a location, go there
            moveOid = _body.location;

        } else {
            // otherwise head to the default lobby to start things off
            MiCasaBootstrapData data = (MiCasaBootstrapData)
                client.getBootstrapData();
            moveOid = data.defLobbyOid;
        }

        if (moveOid > 0) {
            _ctx.getLocationDirector().moveTo(moveOid);
        }
    }

    // documentation inherited
    public void clientDidLogoff (Client client)
    {
        Log.info("Client did logoff [client=" + client + "].");
    }

    protected MiCasaContext _ctx;
    protected MiCasaFrame _frame;
    protected BodyObject _body;

    // our panels
    protected LogonPanel _logonPanel;
}
