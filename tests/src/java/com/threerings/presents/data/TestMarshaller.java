//
// $Id: TestMarshaller.java,v 1.1 2002/08/14 19:07:59 mdb Exp $

package com.threerings.presents.data;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.TestService;
import com.threerings.presents.client.TestService.TestFuncListener;
import com.threerings.presents.client.TestService.TestOidListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

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
        public static final int TEST_SUCCEEDED = 0;

        // documentation inherited from interface
        public void testSucceeded (String arg1, int arg2)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, TEST_SUCCEEDED,
                               new Object[] { arg1, new Integer(arg2) }));
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
            }
        }
    }

    // documentation inherited
    public static class TestOidMarshaller extends ListenerMarshaller
        implements TestOidListener
    {
        /** The method id used to dispatch {@link #gotTestOid}
         * responses. */
        public static final int GOT_TEST_OID = 0;

        // documentation inherited from interface
        public void gotTestOid (int arg1)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, GOT_TEST_OID,
                               new Object[] { new Integer(arg1) }));
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
            }
        }
    }

    /** The method id used to dispatch {@link #test} requests. */
    public static final int TEST = 1;

    // documentation inherited from interface
    public void test (Client arg1, String arg2, int arg3, TestFuncListener arg4)
    {
        TestFuncMarshaller listener4 = new TestFuncMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, TEST, new Object[] {
            arg2, new Integer(arg3), listener4
        });
    }

    /** The method id used to dispatch {@link #getTestOid} requests. */
    public static final int GET_TEST_OID = 2;

    // documentation inherited from interface
    public void getTestOid (Client arg1, TestOidListener arg2)
    {
        TestOidMarshaller listener2 = new TestOidMarshaller();
        listener2.listener = arg2;
        sendRequest(arg1, GET_TEST_OID, new Object[] {
            listener2
        });
    }

    // Class file generated on 14:28:55 08/12/02.
}
