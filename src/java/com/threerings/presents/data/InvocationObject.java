//
// $Id: InvocationObject.java,v 1.3 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.data;

import com.threerings.presents.dobj.DObject;

/**
 * A single invocation object is created by the server invocation manager
 * and is used to receive invocation request messages from the client. The
 * server presently delivers invocation messages to the client via the
 * client object.
 */
public class InvocationObject extends DObject
{
    /**
     * This constant is used to identify invocation requests sent to the
     * server.
     */
    public static final String REQUEST_NAME = "invreq";

    /**
     * This constant is used to identify invocation responses sent to the
     * client.
     */
    public static final String RESPONSE_NAME = "invrsp";

    /**
     * This constant is used to identify invocation notifications sent to
     * the client.
     */
    public static final String NOTIFICATION_NAME = "invnot";
}
