//
// $Id: BootstrapData.java,v 1.7 2002/08/14 19:07:56 mdb Exp $

package com.threerings.presents.net;

import com.threerings.presents.dobj.DObject;
import com.threerings.util.StreamableArrayList;

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

    /** A list of handles to invocation services. */
    public StreamableArrayList services;
}
