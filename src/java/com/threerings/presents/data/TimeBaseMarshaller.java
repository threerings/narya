//
// $Id: TimeBaseMarshaller.java,v 1.1 2002/08/14 19:07:55 mdb Exp $

package com.threerings.presents.data;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.TimeBaseService;
import com.threerings.presents.client.TimeBaseService.GotTimeBaseListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link TimeBaseService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class TimeBaseMarshaller extends InvocationMarshaller
    implements TimeBaseService
{
    // documentation inherited
    public static class GotTimeBaseMarshaller extends ListenerMarshaller
        implements GotTimeBaseListener
    {
        /** The method id used to dispatch {@link #gotTimeOid}
         * responses. */
        public static final int GOT_TIME_OID = 0;

        // documentation inherited from interface
        public void gotTimeOid (int arg1)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, GOT_TIME_OID,
                               new Object[] { new Integer(arg1) }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case GOT_TIME_OID:
                ((GotTimeBaseListener)listener).gotTimeOid(
                    ((Integer)args[0]).intValue());
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    /** The method id used to dispatch {@link #getTimeOid} requests. */
    public static final int GET_TIME_OID = 1;

    // documentation inherited from interface
    public void getTimeOid (Client arg1, String arg2, GotTimeBaseListener arg3)
    {
        GotTimeBaseMarshaller listener3 = new GotTimeBaseMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, GET_TIME_OID, new Object[] {
            arg2, listener3
        });
    }

    // Class file generated on 00:26:01 08/11/02.
}
