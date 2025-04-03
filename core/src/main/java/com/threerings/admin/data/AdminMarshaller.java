//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.data;

import javax.annotation.Generated;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;

import com.threerings.admin.client.AdminService;

/**
 * Provides the implementation of the {@link AdminService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from AdminService.java.")
public class AdminMarshaller extends InvocationMarshaller<ClientObject>
    implements AdminService
{
    /**
     * Marshalls results to implementations of {@code AdminService.ConfigInfoListener}.
     */
    public static class ConfigInfoMarshaller extends ListenerMarshaller
        implements ConfigInfoListener
    {
        /** The method id used to dispatch {@link #gotConfigInfo}
         * responses. */
        public static final int GOT_CONFIG_INFO = 1;

        // from interface ConfigInfoMarshaller
        public void gotConfigInfo (String[] arg1, int[] arg2)
        {
            sendResponse(GOT_CONFIG_INFO, new Object[] { arg1, arg2 });
        }

        @Override // from InvocationMarshaller
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case GOT_CONFIG_INFO:
                ((ConfigInfoListener)listener).gotConfigInfo(
                    (String[])args[0], (int[])args[1]);
                return;

            default:
                super.dispatchResponse(methodId, args);
                return;
            }
        }
    }

    /** The method id used to dispatch {@link #getConfigInfo} requests. */
    public static final int GET_CONFIG_INFO = 1;

    // from interface AdminService
    public void getConfigInfo (AdminService.ConfigInfoListener arg1)
    {
        AdminMarshaller.ConfigInfoMarshaller listener1 = new AdminMarshaller.ConfigInfoMarshaller();
        listener1.listener = arg1;
        sendRequest(GET_CONFIG_INFO, new Object[] {
            listener1
        });
    }
}
