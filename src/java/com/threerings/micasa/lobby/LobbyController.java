//
// $Id: LobbyController.java,v 1.8 2001/10/25 23:42:33 mdb Exp $

package com.threerings.micasa.lobby;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.client.*;
import com.threerings.parlor.game.GameConfig;

import com.threerings.micasa.Log;
import com.threerings.micasa.util.MiCasaContext;

public class LobbyController
    extends PlaceController
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
                    _ctx.getParlorDirector().invite(invitee, config, this);
                } catch (Exception e) {
                    Log.warning("Error instantiating game config.");
                    Log.logStackTrace(e);
                }
            }

        } catch (SecurityException se) {
            // nothing to see here, move it along...
        }
    }

    public void invitationReceived (int inviteId, String inviter,
                                    GameConfig config)
    {
        Log.info("Invitation received [inviteId=" + inviteId +
                 ", inviter=" + inviter + ", config=" + config + "].");

        // accept the invitation. we're game...
        _ctx.getParlorDirector().accept(inviteId);
    }

    public void invitationCancelled (int inviteId)
    {
        Log.info("Invitation cancelled [inviteId=" + inviteId + "].");
    }

    public void invitationAccepted (int inviteId)
    {
        Log.info("Invitation accepted [inviteId=" + inviteId + "].");
    }

    public void invitationRefused (int inviteId, String message)
    {
        Log.info("Invitation refused [inviteId=" + inviteId +
                 ", message=" + message + "].");
    }

    public void invitationCountered (int inviteId, GameConfig config)
    {
        Log.info("Invitation countered [inviteId=" + inviteId +
                 ", config=" + config + "].");
    }

    protected MiCasaContext _ctx;
    protected LobbyConfig _config;
}
