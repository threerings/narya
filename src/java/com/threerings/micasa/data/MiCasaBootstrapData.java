//
// $Id: MiCasaBootstrapData.java,v 1.1 2001/10/09 18:20:08 mdb Exp $

package com.threerings.micasa.data;

import com.threerings.cocktail.cher.net.BootstrapData;

/**
 * Extends the basic Cher bootstrap data and provides some bootstrap
 * information specific to the MiCasa services.
 */
public class MiCasaBootstrapData extends BootstrapData
{
    /** The oid of the default lobby. */
    public int defLobbyOid;

    // documentation inherited
    public void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", defLobbyOid=").append(defLobbyOid);
    }
}
