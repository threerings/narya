//
// $Id: BootstrapData.java,v 1.5 2002/05/28 21:56:38 mdb Exp $

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

    /** The time from which server ticks are incrementing. */
    public long serverStartStamp;
}
