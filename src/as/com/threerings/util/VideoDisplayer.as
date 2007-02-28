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
    public static const SIZE_KNOWN :String = "videoDisplayerSizeKnown";

    /** A value event sent when there's an error loading the video.
     * Value: original error event. */
    public static const VIDEO_ERROR :String = "videoDisplayerError";

    /**
     * Create a video displayer.
     */
    public function VideoDisplayer ()
    {
        _vid = new Video();
        addChild(_vid);

        addEventListener(MouseEvent.ROLL_OVER, handleRollOver);
        addEventListener(MouseEvent.ROLL_OUT, handleRollOut);

        _videoChecker.addEventListener(TimerEvent.TIMER, handleVideoCheck);

        _pauser = new Sprite();
        _pauser.addEventListener(MouseEvent.CLICK, handleClick);
        redrawPauser();
    }

    /**
     * Start playing a video!
     */
    public function setup (url :String) :void
    {
        _netCon = new NetConnection();
        _netCon.addEventListener(NetStatusEvent.NET_STATUS, handleNetStatus);

        // error handlers
        _netCon.addEventListener(AsyncErrorEvent.ASYNC_ERROR, handleAsyncError);
        _netCon.addEventListener(IOErrorEvent.IO_ERROR, handleIOError);
        _netCon.addEventListener(SecurityErrorEvent.SECURITY_ERROR, handleSecurityError);

        _netCon.connect(null);
        _netStream = new NetStream(_netCon);
        // pass in refs to some of our protected methods
        _netStream.client = {
            onMetaData: metaDataReceived
        };
        _netStream.addEventListener(NetStatusEvent.NET_STATUS, handleStreamNetStatus);

        _vid.attachNetStream(_netStream);
        _videoChecker.start();
        _netStream.play(url);
        _paused = false;
    }

    /**
     * Stop playing our video.
     */
    public function shutdown () :void
    {
        _videoChecker.reset();
        _vid.attachNetStream(null);

        if (_netStream != null) {
            _netStream.close();
            _netStream.removeEventListener(NetStatusEvent.NET_STATUS, handleStreamNetStatus);
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
        redrawPauser();

        // tell any interested parties
        dispatchEvent(new ValueEvent(SIZE_KNOWN, [ _vid.videoWidth, _vid.videoHeight ]));
    }

    protected function handleRollOver (event :MouseEvent) :void
    {
        addChild(_pauser);
    }

    protected function handleRollOut (event :MouseEvent) :void
    {
        if (_pauser.parent) {
            removeChild(_pauser);
        }
    }

    protected function handleClick (event :MouseEvent) :void
    {
        // the buck stops here!
        event.stopImmediatePropagation();

        if (_paused) {
            _netStream.resume();
            _paused = false;

        } else {
            _netStream.pause();
            _paused = true;
        }
        redrawPauser();
    }

    /**
     * Draw the pauser sprite, update it's location.
     * This will become something artistic, etc.
     */
    protected function redrawPauser () :void
    {
        with (_pauser.graphics) {
            clear();
            // draw a nice circle
            beginFill(0x333333);
            drawCircle(0, 0, 20);
            endFill();
            lineStyle(2, 0);
            drawCircle(0, 0, 20);

            if (_paused) {
                lineStyle(0, 0, 0);
                beginFill(0x00FF00);
                moveTo(-4, -10);
                lineTo(4, 0);
                lineTo(-4, 10);
                lineTo(-4, -10);
                endFill();

            } else {
                lineStyle(2, 0x00FF00);
                moveTo(-4, -10);
                lineTo(-4, 10);
                moveTo(4, -10);
                lineTo(4, 10);
            }
        }

        // and update the location
        _pauser.x = _vid.width/2;
        _pauser.y = _vid.height/2;
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
        case "NetConnection.Connect.Closed":
            // these status events we ignore
            break;

        default:
            trace("NetStatus status: " + info.code);
            break;
        }
    }

    protected function handleStreamNetStatus (event :NetStatusEvent) :void
    {
        if (event.info.code == "NetStream.Play.Stop") {
            _netStream.seek(0);
            _netStream.pause();
            _paused = true;
            redrawPauser();
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

    /**
     * Redispatch some error we received to our listeners.
     */
    protected function redispatchError (event :Event) :void
    {
        dispatchEvent(new ValueEvent(VIDEO_ERROR, event));
    }

    /**
     * Called when metadata (if any) is found in the video stream.
     */
    protected function metaDataReceived (obj :Object) :void
    {
        trace("Got video metadata:");
        for (var n :String in obj) {
            trace("    " + n + ": " + obj[n]);
        }
    }
    
    protected var _vid :Video;

    protected var _paused :Boolean = false;

    protected var _netCon :NetConnection;

    protected var _netStream :NetStream;

    protected var _pauser :Sprite;

    /** Checks the video every 100ms to see if the dimensions are now know.
     * Yes, this is how to do it. We could trigger on ENTER_FRAME, but then
     * we may not know the dimensions unless we're added on the display list,
     * and we want this to work in the general case. */
    protected var _videoChecker :Timer = new Timer(100);
}
}
