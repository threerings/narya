//
// $Id: TestDecoder.java,v 1.1 2002/08/14 19:07:59 mdb Exp $

package com.threerings.presents.client;

import com.threerings.presents.client.InvocationDecoder;
import com.threerings.presents.client.TestReceiver;

/**
 * Dispatches calls to a {@link TestReceiver} instance.
 */
public class TestDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static final String RECEIVER_CODE = "b4b66d24b85d870d04c8da3524c188eb";

    /** The method id used to dispatch {@link TestReceiver#receivedTest}
     * notifications. */
    public static final int RECEIVED_TEST = 1;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public TestDecoder (TestReceiver receiver)
    {
        this.receiver = receiver;
    }

    // documentation inherited
    public String getReceiverCode ()
    {
        return RECEIVER_CODE;
    }

    // documentation inherited
    public void dispatchNotification (int methodId, Object[] args)
    {
        switch (methodId) {
        case RECEIVED_TEST:
            ((TestReceiver)receiver).receivedTest(
                ((Integer)args[0]).intValue(), (String)args[1]
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
        }
    }

    // Generated on 12:14:10 08/12/02.
}
