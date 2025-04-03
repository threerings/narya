//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.peer.data;

import com.threerings.util.Name;

import com.threerings.presents.peer.data.ClientInfo;

/**
 * Extends the standard {@link ClientInfo} with Crowd bits.
 */
public class CrowdClientInfo extends ClientInfo
{
    /** The client's visible name, which is used for chatting. */
    public Name visibleName;
}
