//
// $Id: BootstrapData.java,v 1.3 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.net;

import com.threerings.presents.dobj.DObject;

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
