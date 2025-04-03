//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.client;

import com.threerings.crowd.util.CrowdContext;

/**
 * Handles the basic bits for our simple chat room.
 */
public class JabberController extends PlaceController
{
    @Override
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        return new JabberPanel(ctx);
    }
}
