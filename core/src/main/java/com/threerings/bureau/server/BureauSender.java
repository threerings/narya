//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;

import com.threerings.bureau.client.BureauDecoder;
import com.threerings.bureau.client.BureauReceiver;

/**
 * Used to issue notifications to a {@link BureauReceiver} instance on a
 * client.
 */
public class BureauSender extends InvocationSender
{
    /**
     * Issues a notification that will result in a call to {@link
     * BureauReceiver#createAgent} on a client.
     */
    public static void createAgent (
        ClientObject target, int arg1)
    {
        sendNotification(
            target, BureauDecoder.RECEIVER_CODE, BureauDecoder.CREATE_AGENT,
            new Object[] { Integer.valueOf(arg1) });
    }

    /**
     * Issues a notification that will result in a call to {@link
     * BureauReceiver#destroyAgent} on a client.
     */
    public static void destroyAgent (
        ClientObject target, int arg1)
    {
        sendNotification(
            target, BureauDecoder.RECEIVER_CODE, BureauDecoder.DESTROY_AGENT,
            new Object[] { Integer.valueOf(arg1) });
    }

}
