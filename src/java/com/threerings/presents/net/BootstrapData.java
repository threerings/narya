//
// $Id: BootstrapData.java,v 1.4 2002/02/04 01:47:20 mdb Exp $

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
}
