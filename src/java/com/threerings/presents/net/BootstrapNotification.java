//
// $Id: BootstrapNotification.java,v 1.7 2004/08/27 02:20:22 mdb Exp $
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

package com.threerings.presents.net;

/**
 * A bootstrap notification is delivered to the client once the server has
 * fully initialized itself in preparation for dealing with this client.
 * The authentication process completes very early and further information
 * need be communicated to the client so that it can fully interact with
 * the server. This information is communicated via the bootstrap
 * notification.
 */
public class BootstrapNotification extends DownstreamMessage
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public BootstrapNotification ()
    {
        super();
    }

    /**
     * Constructs an bootstrap notification with the supplied data.
     */
    public BootstrapNotification (BootstrapData data)
    {
        _data = data;
    }

    public BootstrapData getData ()
    {
        return _data;
    }

    public String toString ()
    {
        return "[type=BOOT, msgid=" + messageId + ", data=" + _data + "]";
    }

    /** The data associated with this notification. */
    protected BootstrapData _data;
}
