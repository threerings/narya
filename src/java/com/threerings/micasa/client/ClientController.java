//
// $Id: ClientController.java,v 1.2 2001/10/04 23:41:44 mdb Exp $

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
import com.threerings.cocktail.party.util.PlaceViewUtil;

import com.threerings.media.sprite.SpriteManager;

import com.threerings.micasa.Log;
import com.threerings.micasa.lobby.LobbyService;
import com.threerings.micasa.util.MiCasaContext;

/**
 * The client controller is responsible for top-level control of the
 * client user interface. It deals with locating and displaying the proper
 * panels and controllers for particular user interface modes (walking
 * around the world, puzzling, etc.) and it manages the side-bar controls
 * as well.
 */
public class ClientController
    extends Controller
    implements ClientObserver, LocationObserver, OccupantObserver, Subscriber
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

        // we want to know about location changes
        _ctx.getLocationDirector().addLocationObserver(this);

        // we also want to know about occupant changes
        _ctx.getOccupantManager().addOccupantObserver(this);

        // create our lobby panel which we'll use once we're logged on
        _lobbyPanel = new LobbyPanel(ctx);

        // create the logon panel and display it
        _logonPanel = new LogonPanel(_ctx);
//          _frame.setPanel(_logonPanel);

        // create a sprite manager
        _spritemgr = new SpriteManager();

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
        Log.info("Got action: " + action);
        return false;
    }

    // documentation inherited
    public void clientDidLogon (Client client)
    {
        Log.info("Client did logon [client=" + client + "].");

        // keep the body object around for stuff
        _body = (BodyObject)client.getClientObject();

        // we're logged on, so we lose the login panel and move to our
        // primary display
        _frame.setPanel(_lobbyPanel);
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

    // documentation inherited
    public boolean locationMayChange (int placeId)
    {
        return true;
    }

    // documentation inherited
    public void locationDidChange (PlaceObject place)
    {
        Log.info("Moved to new location [place=" + place + "].");

        // clean up after the old place
        if (_place != null) {
            _place.removeSubscriber(this);
            PlaceViewUtil.dispatchDidLeavePlace(_frame, _place);
        }

        _place = place;

        // and enter the new place with bells on
        if (_place != null) {
            _place.addSubscriber(this);
            PlaceViewUtil.dispatchWillEnterPlace(_frame, _place);
        }
    }

    // documentation inherited
    public void locationChangeFailed (int placeId, String reason)
    {
        Log.info("Location change failed [plid=" + placeId +
                 ", reason=" + reason + "].");
    }

    public void occupantEntered (OccupantInfo info)
    {
        Log.info("New occupant " + info + ".");
    }

    public void occupantLeft (OccupantInfo info)
    {
    }

    public void occupantUpdated (OccupantInfo info)
    {
    }

    public void objectAvailable (DObject object)
    {
        // don't care
    }

    public void requestFailed (int oid, ObjectAccessException cause)
    {
        // don't care
    }

    public boolean handleEvent (DEvent event, DObject target)
    {
        return true;
    }

    protected MiCasaContext _ctx;
    protected MiCasaFrame _frame;
    protected BodyObject _body;
    protected PlaceObject _place;

    // managers
    protected SpriteManager _spritemgr;

    // our panels
    protected LogonPanel _logonPanel;
    protected LobbyPanel _lobbyPanel;
}
