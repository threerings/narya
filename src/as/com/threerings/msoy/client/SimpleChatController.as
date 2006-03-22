package com.threerings.msoy.client {

import com.threerings.crowd.client.CrowdContext;
import com.threerings.crowd.client.PlaceController;

public class SimpleChatController extends PlaceController
{
    // documentation inherited
    protected override function createPlaceView (ctx :CrowdContext) :PlaceView
    {
        return new SimpleChatPanel(ctx as MsoyContext);
    }
}
}
