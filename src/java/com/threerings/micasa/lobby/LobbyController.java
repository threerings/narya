//
// $Id: LobbyController.java,v 1.1 2001/10/09 00:48:34 mdb Exp $

package com.threerings.micasa.lobby;

import com.threerings.cocktail.party.data.PlaceConfig;
import com.threerings.cocktail.party.client.PlaceController;
import com.threerings.cocktail.party.client.PlaceView;
import com.threerings.cocktail.party.util.PartyContext;

import com.threerings.micasa.Log;

public class LobbyController extends PlaceController
{
    public void init (PartyContext ctx, PlaceConfig config)
    {
        super.init(ctx, config);

        Log.info("Lobby controller created [config=" + config + "].");
    }

    protected PlaceView createPlaceView ()
    {
        return null;
    }
}
