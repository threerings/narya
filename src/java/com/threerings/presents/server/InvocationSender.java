//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.server;

import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;
import com.threerings.presents.client.InvocationReceiver.Registration;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.InvocationNotificationEvent;

/**
 * Provides basic functionality used by all invocation sender classes.
 */
public abstract class InvocationSender
{
    /**
     * Requests that the specified invocation notification be packaged up
     * and sent to the supplied target client.
     */
    public static void sendNotification (
        ClientObject target, String receiverCode, int methodId, Object[] args)
    {
        // convert the receiver hash id into the code used on this
        // specific client
        Registration rreg = (Registration)target.receivers.get(receiverCode);
        if (rreg == null) {
            Log.warning("Unable to locate receiver for invocation " +
                        "service notification [clobj=" + target.who() +
                        ", code=" + receiverCode + ", methId=" + methodId +
                        ", args=" + StringUtil.toString(args) + "].");
            Thread.dumpStack();

        } else {
//             Log.info("Sending notification [target=" + target +
//                      ", code=" + receiverCode + ", methodId=" + methodId +
//                      ", args=" + StringUtil.toString(args) + "].");

            // create and dispatch an invocation notification event
            target.postEvent(
                new InvocationNotificationEvent(
                    target.getOid(), rreg.receiverId, methodId, args));
        }
    }
}
