//
// $Id: LobbyController.java,v 1.3 2001/10/09 18:20:08 mdb Exp $

package com.threerings.micasa.lobby;

import com.threerings.cocktail.party.data.PlaceConfig;
import com.threerings.cocktail.party.client.PlaceController;
import com.threerings.cocktail.party.client.PlaceView;
import com.threerings.cocktail.party.util.PartyContext;

import com.threerings.micasa.Log;
import com.threerings.micasa.util.MiCasaContext;

public class LobbyController extends PlaceController
{
    public void init (PartyContext ctx, PlaceConfig config)
    {
        // cast our context reference
        _ctx = (MiCasaContext)ctx;

        super.init(ctx, config);

        Log.info("Lobby controller created [config=" + config + "].");
    }

    protected PlaceView createPlaceView ()
    {
        return new LobbyPanel(_ctx);
    }

    protected MiCasaContext _ctx;
}
