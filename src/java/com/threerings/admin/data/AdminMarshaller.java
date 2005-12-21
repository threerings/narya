//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.admin.data;

import com.threerings.admin.client.AdminService;
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
        public static final int GOT_CONFIG_INFO = 1;

        // documentation inherited from interface
        public void gotConfigInfo (String[] arg1, int[] arg2)
        {
            _invId = null;
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
    public void getConfigInfo (Client arg1, AdminService.ConfigInfoListener arg2)
    {
        AdminMarshaller.ConfigInfoMarshaller listener2 = new AdminMarshaller.ConfigInfoMarshaller();
        listener2.listener = arg2;
        sendRequest(arg1, GET_CONFIG_INFO, new Object[] {
            listener2
        });
    }

}
