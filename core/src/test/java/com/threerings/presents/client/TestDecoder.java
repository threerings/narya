//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

import com.threerings.presents.client.InvocationDecoder;

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

    @Override
    public String getReceiverCode ()
    {
        return RECEIVER_CODE;
    }

    @Override
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
            return;
        }
    }
}
