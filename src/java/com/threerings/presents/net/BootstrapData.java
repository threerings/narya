//
// $Id: BootstrapData.java,v 1.1 2001/07/19 07:09:16 mdb Exp $

package com.threerings.cocktail.cher.net;

import com.threerings.cocktail.cher.dobj.DObject;

/**
 * An <code>BootstrapData</code> object is communicated back to the client
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

    public String toString ()
    {
        return "[clientOid=" + clientOid + ", invOid=" + invOid + "]";
    }
}
