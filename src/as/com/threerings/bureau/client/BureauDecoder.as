//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2008 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.bureau.client {

import com.threerings.bureau.client.BureauReceiver;
import com.threerings.presents.client.InvocationDecoder;

/**
 * Dispatches calls to a {@link BureauReceiver} instance.
 */
public class BureauDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static const RECEIVER_CODE :String = "3e98f7a30deb5a8e25e05c71c6081bf4";

    /** The method id used to dispatch {@link BureauReceiver#createAgent}
     * notifications. */
    public static const CREATE_AGENT :int = 1;

    /** The method id used to dispatch {@link BureauReceiver#destroyAgent}
     * notifications. */
    public static const DESTROY_AGENT :int = 2;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public function BureauDecoder (receiver :BureauReceiver)
    {
        this.receiver = receiver;
    }

    // documentation inherited
    public override function getReceiverCode () :String
    {
        return RECEIVER_CODE;
    }

    // documentation inherited
    public override function dispatchNotification (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case CREATE_AGENT:
            BureauReceiver(receiver).createAgent(
                args[0] as int
            );
            return;

        case DESTROY_AGENT:
            BureauReceiver(receiver).destroyAgent(
                args[0] as int
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
            return;
        }
    }
}
}
