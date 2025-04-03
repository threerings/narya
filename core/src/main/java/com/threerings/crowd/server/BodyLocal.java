//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.server;

import com.threerings.presents.server.ClientLocal;

import com.threerings.crowd.data.BodyObject;

/**
 * Contains information tracked for each body by the server.
 */
public class BodyLocal extends ClientLocal
{
    /**
     * The time at which the {@link BodyObject#status} field was last updated.
     */
    public long statusTime;
}
