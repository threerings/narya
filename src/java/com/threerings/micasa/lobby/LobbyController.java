//
// $Id: LobbyController.java,v 1.4 2001/10/09 20:22:51 mdb Exp $

package com.threerings.micasa.lobby;

import com.threerings.cocktail.party.data.PlaceConfig;
import com.threerings.cocktail.party.data.PlaceObject;
import com.threerings.cocktail.party.client.PlaceController;
import com.threerings.cocktail.party.client.PlaceView;
import com.threerings.cocktail.party.util.PartyContext;

import com.threerings.parlor.client.*;
import com.threerings.parlor.data.GameConfig;

import com.threerings.micasa.Log;
import com.threerings.micasa.util.MiCasaContext;

public class LobbyController
    extends PlaceController
    implements InvitationHandler, InvitationResponseObserver
{
    public void init (PartyContext ctx, PlaceConfig config)
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
        return new LobbyPanel(_ctx);
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        // if we're testing and a system property has been specified
        // requesting that we invite another player, do so now
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
