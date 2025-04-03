//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.data;

import javax.annotation.Generated;

import com.threerings.presents.client.TimeBaseService;

/**
 * Provides the implementation of the {@link TimeBaseService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from TimeBaseService.java.")
public class TimeBaseMarshaller extends InvocationMarshaller<ClientObject>
    implements TimeBaseService
{
    /**
     * Marshalls results to implementations of {@code TimeBaseService.GotTimeBaseListener}.
     */
    public static class GotTimeBaseMarshaller extends ListenerMarshaller
        implements GotTimeBaseListener
    {
        /** The method id used to dispatch {@link #gotTimeOid}
         * responses. */
        public static final int GOT_TIME_OID = 1;

        // from interface GotTimeBaseMarshaller
        public void gotTimeOid (int arg1)
        {
            sendResponse(GOT_TIME_OID, new Object[] { Integer.valueOf(arg1) });
        }

        @Override // from InvocationMarshaller
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case GOT_TIME_OID:
                ((GotTimeBaseListener)listener).gotTimeOid(
                    ((Integer)args[0]).intValue());
                return;

            default:
                super.dispatchResponse(methodId, args);
                return;
            }
        }
    }

    /** The method id used to dispatch {@link #getTimeOid} requests. */
    public static final int GET_TIME_OID = 1;

    // from interface TimeBaseService
    public void getTimeOid (String arg1, TimeBaseService.GotTimeBaseListener arg2)
    {
        TimeBaseMarshaller.GotTimeBaseMarshaller listener2 = new TimeBaseMarshaller.GotTimeBaseMarshaller();
        listener2.listener = arg2;
        sendRequest(GET_TIME_OID, new Object[] {
            arg1, listener2
        });
    }
}
