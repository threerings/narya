//
// $Id: MiCasaClient.java,v 1.1 2001/10/09 18:20:08 mdb Exp $

package com.threerings.micasa.server;

import com.threerings.cocktail.cher.net.BootstrapData;
import com.threerings.cocktail.cher.server.CherClient;

/**
 * Extends the cher client and provides bootstrap data specific to the
 * MiCasa services.
 */
public class MiCasaClient extends CherClient
{
    // documentation inherited
    protected BootstrapData createBootstrapData ()
    {
        return new BootstrapData();
    }

    // documentation inherited
    protected void populateBootstrapData (BootstrapData data)
    {
        super.populateBootstrapData(data);
    }
}
