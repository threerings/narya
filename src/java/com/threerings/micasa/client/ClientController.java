//
// $Id: ClientController.java,v 1.6 2001/10/09 19:23:26 mdb Exp $

package com.threerings.micasa.client;

import java.awt.event.ActionEvent;
import com.samskivert.swing.Controller;
import com.samskivert.util.StringUtil;

import com.threerings.cocktail.cher.dobj.*;
import com.threerings.cocktail.cher.client.*;
import com.threerings.cocktail.cher.net.Credentials;
import com.threerings.cocktail.cher.net.UsernamePasswordCreds;

import com.threerings.cocktail.party.client.*;
import com.threerings.cocktail.party.data.*;

import com.threerings.micasa.Log;
import com.threerings.micasa.data.MiCasaBootstrapData;
import com.threerings.micasa.util.MiCasaContext;

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
    public ClientController (MiCasaContext ctx, MiCasaFrame frame)
    {
        // we'll want to keep these around
        _ctx = ctx;
        _frame = frame;

        // we want to know about logon/logoff
        _ctx.getClient().addObserver(this);

        // create the logon panel and display it
        _logonPanel = new LogonPanel(_ctx);
//          _frame.setPanel(_logonPanel);

        // configure the client with some credentials and logon
        String username = "bob" +
            ((int)(Math.random() * Integer.MAX_VALUE) % 500);
        Credentials creds = new UsernamePasswordCreds(username, "test");
        Client client = _ctx.getClient();
        client.setCredentials(creds);
        client.logon();
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

        // head to the default lobby to start things off
        MiCasaBootstrapData data = (MiCasaBootstrapData)
            client.getBootstrapData();
        _ctx.getLocationDirector().moveTo(data.defLobbyOid);
    }

    // documentation inherited
    public void clientFailedToLogon (Client client, Exception cause)
    {
        Log.info("Client failed to logon [client=" + client +
                 ", cause=" + cause + "].");
        _logonPanel.logonFailed(cause);
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
        System.exit(0);
    }

    protected MiCasaContext _ctx;
    protected MiCasaFrame _frame;
    protected BodyObject _body;
    protected PlaceObject _place;

    // our panels
    protected LogonPanel _logonPanel;
}
