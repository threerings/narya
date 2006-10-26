package com.threerings.util {

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Loader;
import flash.display.LoaderInfo;
import flash.display.Shape;

import flash.errors.IOError;

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.IEventDispatcher;
import flash.events.IOErrorEvent;
import flash.events.MouseEvent;
import flash.events.NetStatusEvent;
import flash.events.ProgressEvent;
import flash.events.SecurityErrorEvent;
import flash.events.StatusEvent;
import flash.events.TextEvent;

import flash.geom.Point;

import flash.media.Video;

import flash.system.ApplicationDomain;
import flash.system.LoaderContext;
import flash.system.SecurityDomain;

import flash.net.URLRequest;

import mx.core.ScrollPolicy;
import mx.core.UIComponent;

import mx.containers.Box;
import mx.controls.VideoDisplay;

import mx.events.VideoEvent;

import com.threerings.util.StringUtil;

import com.threerings.media.image.ImageUtil;

/**
 * A wrapper class for all media that will be placed on the screen.
 * Subject to change.
 */
public class MediaContainer extends Box
{
    /** A log instance that can be shared by sprites. */
    protected static const log :Log = Log.getLog(MediaContainer);

    /**
     * Constructor.
     */
    public function MediaContainer (url :String = null)
    {
        if (url != null) {
            setMedia(url);
        }

        mouseEnabled = false;
        mouseChildren = true;
        verticalScrollPolicy = ScrollPolicy.OFF;
        horizontalScrollPolicy = ScrollPolicy.OFF;
    }

    /**
     * Get the media. If the media was loaded using a URL, this will
     * likely be the Loader object holding the real media.
     */
    public function getMedia () :DisplayObject
    {
        return _media;
    }

    /**
     * Configure the media to display.
     */
    public function setMedia (url :String) :void
    {
        // shutdown any previous media
        if (_media != null) {
            shutdown(false);
        }

        // set up the new media
        if (StringUtil.endsWith(url.toLowerCase(), ".flv")) {
            setupVideo(url);

        } else {
            setupSwfOrImage(url);
        }
    }

    /**
     * Configure our media as an instance of the specified class.
     */
    public function setMediaClass (clazz :Class) :void
    {
        setMediaObject(new clazz() as DisplayObject);
    }

    /**
     * Configure an already-instantiated DisplayObject as our media.
     */
    public function setMediaObject (disp :DisplayObject) :void
    {
        if (_media != null) {
            shutdown(false);
        }

        if (disp is UIComponent) {
            addChild(disp);
        } else {
            rawChildren.addChild(disp);
        }
        _media = disp;
        updateContentDimensions(disp.width, disp.height);
    }

    /**
     * Configure this sprite to show a video.
     */
    protected function setupVideo (url :String) :void
    {
        var vid :VideoDisplay = new VideoDisplay();
        vid.autoPlay = false;
        _media = vid;
        addChild(vid);
        vid.addEventListener(ProgressEvent.PROGRESS, loadVideoProgress);
        vid.addEventListener(VideoEvent.READY, loadVideoReady);
        vid.addEventListener(VideoEvent.REWIND, videoDidRewind);

        // start it loading
        vid.source = url;
        vid.load();
    }

    /**
     * Configure this sprite to show an image or flash movie.
     */
    protected function setupSwfOrImage (url :String) :void
    {
        // create our loader and set up some event listeners
        var loader :Loader = new Loader();
        _media = loader;
        var info :LoaderInfo = loader.contentLoaderInfo;
        info.addEventListener(Event.COMPLETE, loadingComplete);
        info.addEventListener(IOErrorEvent.IO_ERROR, loadError);
        info.addEventListener(ProgressEvent.PROGRESS, loadProgress);

        // create a mask to prevent the media from drawing out of bounds
        if (maxContentWidth < int.MAX_VALUE &&
                maxContentHeight < int.MAX_VALUE) {
            configureMask(maxContentWidth, maxContentHeight);
        }

        // start it loading, add it as a child
        loader.load(new URLRequest(url), getContext(url));
        rawChildren.addChild(loader);

        try {
            updateContentDimensions(info.width, info.height);
        } catch (err :Error) {
            // an error is thrown trying to access these props before they're
            // ready
        }
    }

    /**
     * Display a 'broken image' to indicate there were troubles with
     * loading the media.
     */
    protected function setupBrokenImage (w :int = -1, h :int = -1) :void
    {
        if (w == -1) {
            w = 100;
        }
        if (h == -1) {
            h = 100;
        }
        _media = ImageUtil.createErrorImage(w, h);
        rawChildren.addChild(_media);
    }

    /**
     * Get the application domain being used by this media, or null if
     * none or not applicable.
     */
    public function getApplicationDomain () :ApplicationDomain
    {
        return (_media is Loader)
            ? (_media as Loader).contentLoaderInfo.applicationDomain
            : null;
    }

    /**
     * Unload the media we're displaying, clean up any resources.
     *
     * @param completely if true, we're going away and should stop
     * everything. Otherwise, we're just loading up new media.
     */
    public function shutdown (completely :Boolean = true) :void
    {
        try {
            // remove the mask
            if (_media != null && _media.mask != null) {
                rawChildren.removeChild(_media.mask);
                _media.mask = null;
            }

            if (_media is Loader) {
                var loader :Loader = (_media as Loader);
                // remove any listeners
                removeListeners(loader.contentLoaderInfo);

                // dispose of media
                try {
                    loader.close();
                } catch (ioe :IOError) {
                    // ignore
                }
                loader.unload();

                rawChildren.removeChild(loader);

            } else if (_media is VideoDisplay) {
                var vid :VideoDisplay = (_media as VideoDisplay);
                // remove any listeners
                vid.removeEventListener(ProgressEvent.PROGRESS,
                    loadVideoProgress);
                vid.removeEventListener(VideoEvent.READY, loadVideoReady);
                vid.removeEventListener(VideoEvent.REWIND, videoDidRewind);

                // dispose of media
                vid.pause();
                try {
                    vid.close();
                } catch (ioe :IOError) {
                    // ignore
                }
                vid.stop();

                // remove from hierarchy
                removeChild(vid);

            } else if (_media != null) {
                if (_media is UIComponent) {
                    removeChild(_media);
                } else {
                    rawChildren.removeChild(_media);
                }
            }
        } catch (ioe :IOError) {
            log.warning("Error shutting down media: " + ioe);
            log.logStackTrace(ioe);
        }

        // clean everything up
        _w = 0;
        _h = 0;
        _media = null;
        width = NaN;
        height = NaN;
    }

    /**
     * Get the width of the content, bounded by the maximum.
     */
    public function get contentWidth () :int
    {
        return Math.min(Math.abs(_w * getMediaScaleX()), maxContentWidth);
    }

    /**
     * Get the height of the content, bounded by the maximum.
     */
    public function get contentHeight () :int
    {
        return Math.min(Math.abs(_h * getMediaScaleY()), maxContentHeight);
    }

    /**
     * Get the maximum allowable width for our content.
     */
    public function get maxContentWidth () :int
    {
        return int.MAX_VALUE;
    }

    /**
     * Get the maximum allowable height for our content.
     */
    public function get maxContentHeight () :int
    {
        return int.MAX_VALUE;
    }

    /**
     * Get the X scaling factor to use on the actual media.
     */
    public function getMediaScaleX () :Number
    {
        return 1;
    }

    /**
     * Get the Y scaling factor to use on the actual media.
     */
    public function getMediaScaleY () :Number
    {
        return 1;
    }

    /**
     * Return the LoaderContext that should be used to load the media
     * at the specified url.
     */
    protected function getContext (url :String) :LoaderContext
    {
        // We allow content to share but not overwrite our classes
        return new LoaderContext(true, 
            new ApplicationDomain(ApplicationDomain.currentDomain),
            SecurityDomain.currentDomain);
    }

    /**
     * Remove our listeners from the LoaderInfo object.
     */
    protected function removeListeners (info :LoaderInfo) :void
    {
        info.removeEventListener(Event.COMPLETE, loadingComplete);
        info.removeEventListener(IOErrorEvent.IO_ERROR, loadError);
        info.removeEventListener(ProgressEvent.PROGRESS, loadProgress);
    }

    /**
     * A callback to receive IO_ERROR events.
     */
    protected function loadError (event :IOErrorEvent) :void
    {
        var info :LoaderInfo = (event.target as LoaderInfo);
        removeListeners(info);

        var loader :Loader = (_media as Loader);
        rawChildren.removeChild(loader);
        if (loader.mask != null) {
            rawChildren.removeChild(loader.mask);
        }

        setupBrokenImage(-1, -1);
    }

    /**
     * A callback to receive PROGRESS events.
     */
    protected function loadProgress (event :ProgressEvent) :void
    {
        updateLoadingProgress(event.bytesLoaded, event.bytesTotal);
        var info :LoaderInfo = (event.target as LoaderInfo);
        try {
            updateContentDimensions(info.width, info.height);
        } catch (err :Error) {
            // an error is thrown trying to access these props before they're
            // ready
        }
    }

    /**
     * A callback to receive PROGRESS events on the video.
     */
    protected function loadVideoProgress (event :ProgressEvent) :void
    {
        var vid :VideoDisplay = (event.currentTarget as VideoDisplay);
        updateContentDimensions(vid.videoWidth, vid.videoHeight);

        updateLoadingProgress(vid.bytesLoaded, vid.bytesTotal);
    }

    /**
     * A callback to receive READY events for video.
     */
    protected function loadVideoReady (event :VideoEvent) :void
    {
        var vid :VideoDisplay = (event.currentTarget as VideoDisplay);
        updateContentDimensions(vid.videoWidth, vid.videoHeight);
        updateLoadingProgress(1, 1);

        vid.play();

        // remove the two listeners
        vid.removeEventListener(ProgressEvent.PROGRESS, loadVideoProgress);
        vid.removeEventListener(VideoEvent.READY, loadVideoReady);
    }

    /**
     * Callback function to receive COMPLETE events for swfs or images.
     */
    protected function loadingComplete (event :Event) :void
    {
        var info :LoaderInfo = (event.target as LoaderInfo);
        removeListeners(info);

//        trace("Loading complete: " + info.url +
//            ", childAllowsParent=" + info.childAllowsParent +
//            ", parentAllowsChild=" + info.parentAllowsChild +
//            ", sameDomain=" + info.sameDomain);

        updateContentDimensions(info.width, info.height);
        updateLoadingProgress(1, 1);
    }

    /**
     * Called when the video auto-rewinds.
     */
    protected function videoDidRewind (event :VideoEvent) :void
    {
        (_media as VideoDisplay).play();
    }

    /**
     * Configure the mask for this object.
     */
    protected function configureMask (ww :int, hh :int) :void
    {
        var mask :Shape;
        if (_media.mask != null) {
            mask = (_media.mask as Shape);

        } else {
            mask = new Shape();
            // the mask must be added to the display list (which is wacky)
            rawChildren.addChild(mask);
            _media.mask = mask;
        }

        mask.graphics.clear();
        mask.graphics.beginFill(0xFFFFFF);
        mask.graphics.drawRect(0, 0, ww, hh);
        mask.graphics.endFill();
    }

    /**
     * Called during loading as we figure out how big the content we're
     * loading is.
     */
    protected function updateContentDimensions (ww :int, hh :int) :void
    {
        width = ww;
        height = hh;

        // update our saved size, and possibly notify our container
        if (_w != ww || _h != hh) {
            _w = ww;
            _h = hh;
            contentDimensionsUpdated();
        }
    }

    /**
     * Called when we know the true size of the content.
     */
    protected function contentDimensionsUpdated () :void
    {
        // nada, by default
    }

    /**
     * Update the graphics to indicate how much is loaded.
     */
    protected function updateLoadingProgress (
            soFar :Number, total :Number) :void
    {
        // nada, by default
    }

    /** The unscaled width of our content. */
    protected var _w :int;

    /** The unscaled height of our content. */
    protected var _h :int;

    /** Either a Loader or a VideoDisplay. */
    protected var _media :DisplayObject;
}
}
