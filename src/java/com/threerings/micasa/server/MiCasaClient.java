//
// $Id: MiCasaClient.java,v 1.3 2001/10/11 04:13:33 mdb Exp $

package com.threerings.micasa.server;

import com.threerings.presents.net.BootstrapData;
import com.threerings.crowd.server.CrowdClient;

import com.threerings.micasa.data.MiCasaBootstrapData;

/**
 * Extends the Crowd client and provides bootstrap data specific to the
 * MiCasa services.
 */
public class MiCasaClient extends CrowdClient
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
