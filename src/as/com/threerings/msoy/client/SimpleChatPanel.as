package com.threerings.msoy.client {

import mx.containers.VBox;

public class SimpleChatPanel extends VBox
    implements PlaceView
{
    public function SimpleChatPanel (ctx :MsoyContext)
    {
        addChild(new ChatTextArea(ctx));
        addChild(new ChatControl(ctx));
    }

    // documentation inherited from interface PlaceView
    public function willEnterPlace (plobj :PlaceObject) :void
    {
    }

    // documentation inherited from interface PlaceView
    public function didLeavePlace (plobj :PlaceObject) :void
    {
    }
}
}
