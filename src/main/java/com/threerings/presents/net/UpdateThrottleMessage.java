//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.net;

/**
 * Notifies the client that its message throttle has been updated.
 */
public class UpdateThrottleMessage extends DownstreamMessage
{
    /** The number of messages allowed per second. */
    public final int messagesPerSec;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public UpdateThrottleMessage ()
    {
        this.messagesPerSec = 0;
    }

    public UpdateThrottleMessage (int messagesPerSec)
    {
        this.messagesPerSec = messagesPerSec;
    }
}
