//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

import java.util.List;

import javax.annotation.Generated;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.TestService;

/**
 * Provides the implementation of the {@link TestService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from TestService.java.")
public class TestMarshaller extends InvocationMarshaller<ClientObject>
    implements TestService
{
    /**
     * Marshalls results to implementations of {@code TestService.TestFuncListener}.
     */
    public static class TestFuncMarshaller extends ListenerMarshaller
        implements TestFuncListener
    {
        /** The method id used to dispatch {@link #testSucceeded}
         * responses. */
        public static final int TEST_SUCCEEDED = 1;

        // from interface TestFuncMarshaller
        public void testSucceeded (String arg1, int arg2)
        {
            sendResponse(TEST_SUCCEEDED, new Object[] { arg1, Integer.valueOf(arg2) });
        }

        @Override // from InvocationMarshaller
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

    /**
     * Marshalls results to implementations of {@code TestService.TestOidListener}.
     */
    public static class TestOidMarshaller extends ListenerMarshaller
        implements TestOidListener
    {
        /** The method id used to dispatch {@link #gotTestOid}
         * responses. */
        public static final int GOT_TEST_OID = 1;

        // from interface TestOidMarshaller
        public void gotTestOid (int arg1)
        {
            sendResponse(GOT_TEST_OID, new Object[] { Integer.valueOf(arg1) });
        }

        @Override // from InvocationMarshaller
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

    // from interface TestService
    public void getTestOid (TestService.TestOidListener arg1)
    {
        TestMarshaller.TestOidMarshaller listener1 = new TestMarshaller.TestOidMarshaller();
        listener1.listener = arg1;
        sendRequest(GET_TEST_OID, new Object[] {
            listener1
        });
    }

    /** The method id used to dispatch {@link #giveMeThePower} requests. */
    public static final int GIVE_ME_THE_POWER = 2;

    // from interface TestService
    public void giveMeThePower (InvocationService.ConfirmListener arg1)
    {
        InvocationMarshaller.ConfirmMarshaller listener1 = new InvocationMarshaller.ConfirmMarshaller();
        listener1.listener = arg1;
        sendRequest(GIVE_ME_THE_POWER, new Object[] {
            listener1
        });
    }

    /** The method id used to dispatch {@link #test} requests. */
    public static final int TEST = 3;

    // from interface TestService
    public void test (String arg1, int arg2, List<Integer> arg3, TestService.TestFuncListener arg4)
    {
        TestMarshaller.TestFuncMarshaller listener4 = new TestMarshaller.TestFuncMarshaller();
        listener4.listener = arg4;
        sendRequest(TEST, new Object[] {
            arg1, Integer.valueOf(arg2), arg3, listener4
        });
    }
}
