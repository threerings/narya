//
// $Id: MiCasaClient.java,v 1.2 2001/10/09 19:23:50 mdb Exp $

package com.threerings.micasa.server;

import com.threerings.cocktail.cher.net.BootstrapData;
import com.threerings.cocktail.party.server.PartyClient;

import com.threerings.micasa.data.MiCasaBootstrapData;

/**
 * Extends the cher client and provides bootstrap data specific to the
 * MiCasa services.
 */
public class MiCasaClient extends PartyClient
{
    // documentation inherited
    protected BootstrapData createBootstrapData ()
    {
        return new MiCasaBootstrapData();
    }

    // documentation inherited
    protected void populateBootstrapData (BootstrapData data)
    {
        super.populateBootstrapData(data);

        // let the client know their default lobby oid
        ((MiCasaBootstrapData)data).defLobbyOid =
            MiCasaServer.lobreg.getDefaultLobbyOid();
    }
}
