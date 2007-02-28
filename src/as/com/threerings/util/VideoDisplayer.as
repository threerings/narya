package com.threerings.util { // TODO: Move.

import flash.display.Sprite;

import flash.events.AsyncErrorEvent;
import flash.events.IOErrorEvent;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.events.NetStatusEvent;
import flash.events.SecurityErrorEvent;
import flash.events.TimerEvent;

import flash.media.Video;

import flash.net.NetConnection;
import flash.net.NetStream;

import flash.utils.Timer;

public class VideoDisplayer extends Sprite
{
    /** A value event dispatched when the size of the video is known.
     * Value: [ width, height ]. */
    public static const SIZE_KNOWN :String = "SIZE_KNOWN";

    /** A value event sent when there's an error loading the video.
     * Value: original error event. */
    public static const VIDEO_ERROR :String = "VIDEO_ERROR";

    public function VideoDisplayer ()
    {
        _vid = new Video();
        addChild(_vid);

        addEventListener(MouseEvent.CLICK, handleClick);
        _videoChecker.addEventListener(TimerEvent.TIMER, handleVideoCheck);
    }

    public function setup (url :String) :void
    {
        _netCon = new NetConnection();
        _netCon.addEventListener(NetStatusEvent.NET_STATUS, handleNetStatus);

        // error handlers
        _netCon.addEventListener(AsyncErrorEvent.ASYNC_ERROR, handleAsyncError);
        _netCon.addEventListener(IOErrorEvent.IO_ERROR, handleIOError);
        _netCon.addEventListener(SecurityErrorEvent.SECURITY_ERROR, handleSecurityError);

        _netCon.connect(null);
        trace("Not waiting, just using!");
        _netStream = new NetStream(_netCon);
        _netStream.client = this;
        //_netStream.client = new Callbacker(this);
        _vid.attachNetStream(_netStream);
        _videoChecker.start();
        _netStream.play(url);
    }

    public function shutdown () :void
    {
        _videoChecker.reset();
        _vid.attachNetStream(null);

        if (_netStream != null) {
            _netStream.close();
            _netStream = null;
        }
        if (_netCon != null) {
            _netCon.close();
            _netCon.removeEventListener(NetStatusEvent.NET_STATUS, handleNetStatus);
            _netCon.removeEventListener(AsyncErrorEvent.ASYNC_ERROR, handleAsyncError);
            _netCon.removeEventListener(IOErrorEvent.IO_ERROR, handleIOError);
            _netCon.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, handleSecurityError);
            _netCon = null;
        }
    }

    /**
     * Check to see if we now know the dimensions of the video.
     */
    protected function handleVideoCheck (event :TimerEvent) :void
    {
        if (_vid.videoWidth == 0 || _vid.videoHeight == 0) {
            return; // not known yet!
        }

        // stop the checker timer
        _videoChecker.stop();

        // set up the width/height
        _vid.width = _vid.videoWidth;
        _vid.height = _vid.videoHeight;

        // tell any interested parties
        dispatchEvent(new ValueEvent(SIZE_KNOWN, [ _vid.videoWidth, _vid.videoHeight ]));
    }

    protected function handleClick (event :MouseEvent) :void
    {
        trace("Click!");
        _netStream.togglePause();
    }

    protected function handleNetStatus (event :NetStatusEvent) :void
    {
        var info :Object = event.info;
        if ("error" == info.level) {
            trace("NetStatus error: " + info.code);
            redispatchError(event);
            return;
        }
        // else info.level == "status"
        switch (info.code) {
        case "NetConnection.Connect.Success":
            trace("Connected!");
            break;

        default:
            trace("NetStatus status: " + info.code);
            break;
        }
    }

    protected function handleAsyncError (event :AsyncErrorEvent) :void
    {
        trace("AsyncError: " + event);
        redispatchError(event);
    }

    protected function handleIOError (event :IOErrorEvent) :void
    {
        trace("IOError: " + event);
        redispatchError(event);
    }

    protected function handleSecurityError (event :SecurityErrorEvent) :void
    {
        trace("SecurityError: " + event);
        redispatchError(event);
    }

    protected function redispatchError (event :Event) :void
    {
        dispatchEvent(new ValueEvent(VIDEO_ERROR, event));
    }

    public function callbackCalled (name :String, args :Array) :*
    {
        trace("Callback: " + name + ", args: " + args);
        return undefined;
    }

    public function onMetaData (obj :Object) :void
    {
        trace("Got metadata..");
        for (var n :String in obj) {
            trace("name " + n + " -> " + obj[n]);
        }
    }

    protected var _vid :Video;

    protected var _netCon :NetConnection;

    protected var _netStream :NetStream;

    /** Checks the video every 100ms to see if the dimensions are now know.
     * Yes, this is how to do it. We could trigger on ENTER_FRAME, but then
     * we may not know the dimensions unless we're added on the display list,
     * and we want this to work in the general case. */
    protected var _videoChecker :Timer = new Timer(100);
}
}

import flash.utils.Proxy;

import flash.utils.flash_proxy;

import com.threerings.util.VideoDisplayer;

use namespace flash_proxy;

/**
 */
internal class Callbacker extends Proxy
{
    public function Callbacker (displayer :VideoDisplayer)
    {
        _displayer = displayer;

        _props
    }

    override flash_proxy function hasProperty (name :*) :Boolean
    {
        return (_props[name] !== undefined);
    }

    override flash_proxy function getProperty (name :*) :*
    {
        trace("Damn thing asked about '" + name + "'.");
        return _props[name];
    }

    override flash_proxy function setProperty (name :*, value :*) :void
    {
        trace("Damn thing tried to set '" + name + "' to '" + value + "'.");
        _props[name] = value;
    }

    override flash_proxy function deleteProperty (name :*) :Boolean
    {
        trace("Damn thing tried to delete '" + name + "'.");
        return (delete _props[name]);
    }

    override flash_proxy function callProperty (name :*, ... args) :*
    {
        return _displayer.callbackCalled(name, args);
    }

    protected var _props :Object = {};

    protected var _displayer :VideoDisplayer;
}
