//
// $Id: LobbyController.java,v 1.10 2004/03/06 11:29:19 mdb Exp $

package com.threerings.micasa.lobby;

import com.threerings.util.Name;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.client.*;
import com.threerings.parlor.game.GameConfig;

import com.threerings.micasa.Log;
import com.threerings.micasa.util.MiCasaContext;

public class LobbyController extends PlaceController
    implements InvitationHandler, InvitationResponseObserver
{
    public void init (CrowdContext ctx, PlaceConfig config)
    {
        // cast our context reference
        _ctx = (MiCasaContext)ctx;
        _config = (LobbyConfig)config;

        super.init(ctx, config);

        // register ourselves as the invitation handler
        _ctx.getParlorDirector().setInvitationHandler(this);
    }

    protected PlaceView createPlaceView ()
    {
        return new LobbyPanel(_ctx, _config);
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        try {
            // if a system property has been specified requesting that we
            // invite another player, do so now
            String invitee = System.getProperty("invitee");
            if (invitee != null) {
                // create a game config object
                try {
                    GameConfig config = _config.getGameConfig();
                    _ctx.getParlorDirector().invite(
                        new Name(invitee), config, this);
                } catch (Exception e) {
                    Log.warning("Error instantiating game config.");
                    Log.logStackTrace(e);
                }
            }

        } catch (SecurityException se) {
            // nothing to see here, move it along...
        }
    }

    // documentation inherited from interface
    public void invitationReceived (Invitation invite)
    {
        Log.info("Invitation received [invite=" + invite + "].");

        // accept the invitation. we're game...
        invite.accept();
    }

    // documentation inherited from interface
    public void invitationCancelled (Invitation invite)
    {
        Log.info("Invitation cancelled " + invite + ".");
    }

    // documentation inherited from interface
    public void invitationAccepted (Invitation invite)
    {
        Log.info("Invitation accepted " + invite + ".");
    }

    // documentation inherited from interface
    public void invitationRefused (Invitation invite, String message)
    {
        Log.info("Invitation refused [invite=" + invite +
                 ", message=" + message + "].");
    }

    // documentation inherited from interface
    public void invitationCountered (Invitation invite, GameConfig config)
    {
        Log.info("Invitation countered [invite=" + invite +
                 ", config=" + config + "].");
    }

    protected MiCasaContext _ctx;
    protected LobbyConfig _config;
}
