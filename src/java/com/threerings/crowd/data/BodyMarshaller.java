//
// $Id: BodyMarshaller.java,v 1.3 2004/02/25 14:41:47 mdb Exp $

package com.threerings.crowd.data;

import com.threerings.crowd.client.BodyService;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;

/**
 * Provides the implementation of the {@link BodyService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class BodyMarshaller extends InvocationMarshaller
    implements BodyService
{
    /** The method id used to dispatch {@link #setIdle} requests. */
    public static final int SET_IDLE = 1;

    // documentation inherited from interface
    public void setIdle (Client arg1, boolean arg2)
    {
        sendRequest(arg1, SET_IDLE, new Object[] {
            new Boolean(arg2)
        });
    }

}
