//
// $Id: AdminMarshaller.java,v 1.1 2002/08/14 19:07:48 mdb Exp $

package com.threerings.admin.data;

import com.threerings.admin.client.AdminService;
import com.threerings.admin.client.AdminService.ConfigInfoListener;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link AdminService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class AdminMarshaller extends InvocationMarshaller
    implements AdminService
{
    // documentation inherited
    public static class ConfigInfoMarshaller extends ListenerMarshaller
        implements ConfigInfoListener
    {
        /** The method id used to dispatch {@link #gotConfigInfo}
         * responses. */
        public static final int GOT_CONFIG_INFO = 0;

        // documentation inherited from interface
        public void gotConfigInfo (String[] arg1, int[] arg2)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, GOT_CONFIG_INFO,
                               new Object[] { arg1, arg2 }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case GOT_CONFIG_INFO:
                ((ConfigInfoListener)listener).gotConfigInfo(
                    (String[])args[0], (int[])args[1]);
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    /** The method id used to dispatch {@link #getConfigInfo} requests. */
    public static final int GET_CONFIG_INFO = 1;

    // documentation inherited from interface
    public void getConfigInfo (Client arg1, ConfigInfoListener arg2)
    {
        ConfigInfoMarshaller listener2 = new ConfigInfoMarshaller();
        listener2.listener = arg2;
        sendRequest(arg1, GET_CONFIG_INFO, new Object[] {
            listener2
        });
    }

    // Class file generated on 00:25:58 08/11/02.
}
