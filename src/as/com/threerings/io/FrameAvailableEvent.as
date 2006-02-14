package com.threerings.io {

import flash.events.Event;

import flash.util.ByteArray;

public class FrameAvailableEvent extends Event
{
    /** The event code for a frame available. */
    public static const FRAME_AVAILABLE :String = "frameAvail";

    public function FrameAvailableEvent (frameData :ByteArray)
    {
        super(FRAME_AVAILABLE);
        _frameData = frameData;
    }

    public function getFrameData () :ByteArray
    {
        return _frameData;
    }

    protected var _frameData :ByteArray;
}
}
