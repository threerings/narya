//
// $Id: SceneSender.java,v 1.3 2004/08/27 02:20:43 mdb Exp $
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

package com.threerings.whirled.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;
import com.threerings.whirled.client.SceneDecoder;
import com.threerings.whirled.client.SceneReceiver;

/**
 * Used to issue notifications to a {@link SceneReceiver} instance on a
 * client.
 *
 * <p> Generated from <code>
 * $Id: SceneSender.java,v 1.3 2004/08/27 02:20:43 mdb Exp $
 * </code>
 */
public class SceneSender extends InvocationSender
{
    /**
     * Issues a notification that will result in a call to {@link
     * SceneReceiver#forcedMove} on a client.
     */
    public static void forcedMove (
        ClientObject target, int arg1)
    {
        sendNotification(
            target, SceneDecoder.RECEIVER_CODE, SceneDecoder.FORCED_MOVE,
            new Object[] { new Integer(arg1) });
    }

    // Generated on 12:37:01 08/20/02.
}
