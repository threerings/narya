//
// $Id: MiCasaBootstrapData.java,v 1.2 2001/10/11 04:13:33 mdb Exp $

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

    // documentation inherited
    public void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", defLobbyOid=").append(defLobbyOid);
    }
}
