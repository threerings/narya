//
// $Id: MiCasaBootstrapData.java,v 1.3 2002/02/04 01:47:20 mdb Exp $

package com.threerings.micasa.data;

import com.threerings.presents.net.BootstrapData;

/**
 * Extends the basic Presents bootstrap data and provides some bootstrap
 * information specific to the MiCasa services.
 */
public class MiCasaBootstrapData extends BootstrapData
{
    /** The oid of the default lobby. */
    public int defLobbyOid;
}
