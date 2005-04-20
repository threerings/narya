//
// $Id$

package com.threerings.crowd.client;

import com.threerings.crowd.util.CrowdContext;

/**
 * Handles the basic bits for our simple chat room.
 */
public class JabberController extends PlaceController
{
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        return new JabberPanel(ctx);
    }
}
