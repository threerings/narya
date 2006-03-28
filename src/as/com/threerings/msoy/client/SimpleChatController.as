package com.threerings.msoy.client {

import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.util.CrowdContext;

public class SimpleChatController extends PlaceController
{
    // documentation inherited
    protected override function createPlaceView (ctx :CrowdContext) :PlaceView
    {
        return new SimpleChatPanel(ctx as MsoyContext);
    }
}
}
