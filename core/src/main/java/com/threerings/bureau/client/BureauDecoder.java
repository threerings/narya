//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.client;

import com.threerings.presents.client.InvocationDecoder;

/**
 * Dispatches calls to a {@link BureauReceiver} instance.
 */
public class BureauDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static final String RECEIVER_CODE = "3e98f7a30deb5a8e25e05c71c6081bf4";

    /** The method id used to dispatch {@link BureauReceiver#createAgent}
     * notifications. */
    public static final int CREATE_AGENT = 1;

    /** The method id used to dispatch {@link BureauReceiver#destroyAgent}
     * notifications. */
    public static final int DESTROY_AGENT = 2;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public BureauDecoder (BureauReceiver receiver)
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
        case CREATE_AGENT:
            ((BureauReceiver)receiver).createAgent(
                ((Integer)args[0]).intValue()
            );
            return;

        case DESTROY_AGENT:
            ((BureauReceiver)receiver).destroyAgent(
                ((Integer)args[0]).intValue()
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
            return;
        }
    }
}
