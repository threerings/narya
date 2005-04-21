//
// $Id$

package com.threerings.jme.client;

import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.util.CrowdContext;

/**
 * Handles the basic bits for our simple chat room.
 */
public class JabberController extends PlaceController
{
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        return new JabberView(ctx);
    }
}
