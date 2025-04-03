//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.peer.data;

import com.threerings.util.Name;

/**
 * Represents an authenticated peer client.
 */
public class PeerAuthName extends Name
{
    public PeerAuthName (String nodeName)
    {
        super(nodeName);
    }
}
