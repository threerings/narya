//
// $Id: BootstrapData.java,v 1.2 2001/10/09 18:17:52 mdb Exp $

package com.threerings.cocktail.cher.net;

import com.threerings.cocktail.cher.dobj.DObject;

/**
 * A <code>BootstrapData</code> object is communicated back to the client
 * after authentication has succeeded and after the server is fully
 * prepared to deal with the client. It contains information the client
 * will need to interact with the server.
 */
public class BootstrapData extends DObject
{
    /** The oid of this client's associated distributed object. */
    public int clientOid;

    /** The oid to which to send invocation requests. */
    public int invOid;

    // documentation inherited
    public void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", clientOid=").append(clientOid);
        buf.append(", invOid=").append(invOid);
    }
}
