//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.data;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.TestService;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import java.util.ArrayList;

/**
 * Provides the implementation of the {@link TestService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class TestMarshaller extends InvocationMarshaller
    implements TestService
{
    // documentation inherited
    public static class TestFuncMarshaller extends ListenerMarshaller
        implements TestFuncListener
    {
        /** The method id used to dispatch {@link #testSucceeded}
         * responses. */
        public static final int TEST_SUCCEEDED = 1;

        // documentation inherited from interface
        public void testSucceeded (String arg1, int arg2)
        {
            _invId = null;
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, TEST_SUCCEEDED,
                               new Object[] { arg1, Integer.valueOf(arg2) }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case TEST_SUCCEEDED:
                ((TestFuncListener)listener).testSucceeded(
                    (String)args[0], ((Integer)args[1]).intValue());
                return;

            default:
                super.dispatchResponse(methodId, args);
                return;
            }
        }
    }

    // documentation inherited
    public static class TestOidMarshaller extends ListenerMarshaller
        implements TestOidListener
    {
        /** The method id used to dispatch {@link #gotTestOid}
         * responses. */
        public static final int GOT_TEST_OID = 1;

        // documentation inherited from interface
        public void gotTestOid (int arg1)
        {
            _invId = null;
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, GOT_TEST_OID,
                               new Object[] { Integer.valueOf(arg1) }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case GOT_TEST_OID:
                ((TestOidListener)listener).gotTestOid(
                    ((Integer)args[0]).intValue());
                return;

            default:
                super.dispatchResponse(methodId, args);
                return;
            }
        }
    }

    /** The method id used to dispatch {@link #getTestOid} requests. */
    public static final int GET_TEST_OID = 1;

    // documentation inherited from interface
    public void getTestOid (Client arg1, TestService.TestOidListener arg2)
    {
        TestMarshaller.TestOidMarshaller listener2 = new TestMarshaller.TestOidMarshaller();
        listener2.listener = arg2;
        sendRequest(arg1, GET_TEST_OID, new Object[] {
            listener2
        });
    }

    /** The method id used to dispatch {@link #test} requests. */
    public static final int TEST = 2;

    // documentation inherited from interface
    public void test (Client arg1, String arg2, int arg3, ArrayList<java.lang.Integer> arg4, TestService.TestFuncListener arg5)
    {
        TestMarshaller.TestFuncMarshaller listener5 = new TestMarshaller.TestFuncMarshaller();
        listener5.listener = arg5;
        sendRequest(arg1, TEST, new Object[] {
            arg2, Integer.valueOf(arg3), arg4, listener5
        });
    }

}
