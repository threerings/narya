//
// $Id: SceneDecoder.java,v 1.3 2004/08/27 02:20:39 mdb Exp $
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

package com.threerings.whirled.client;

import com.threerings.presents.client.InvocationDecoder;
import com.threerings.whirled.client.SceneReceiver;

/**
 * Dispatches calls to a {@link SceneReceiver} instance.
 *
 * <p> Generated from <code>
 * $Id: SceneDecoder.java,v 1.3 2004/08/27 02:20:39 mdb Exp $
 * </code>
 */
public class SceneDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static final String RECEIVER_CODE = "c4d0cf66b81a6e83d119b2d607725651";

    /** The method id used to dispatch {@link SceneReceiver#forcedMove}
     * notifications. */
    public static final int FORCED_MOVE = 1;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public SceneDecoder (SceneReceiver receiver)
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
        case FORCED_MOVE:
            ((SceneReceiver)receiver).forcedMove(
                ((Integer)args[0]).intValue()
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
        }
    }

    // Generated on 12:37:01 08/20/02.
}
