//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.util {

//import flash.display.Bitmap;
import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Loader;
import flash.display.LoaderInfo;
import flash.display.Shape;
import flash.display.Sprite;

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

import flash.text.TextField;
import flash.text.TextFieldAutoSize;

import flash.system.ApplicationDomain;
import flash.system.LoaderContext;
import flash.system.SecurityDomain;

import flash.net.URLRequest;

import com.threerings.util.StringUtil;

import com.threerings.media.image.ImageUtil;

/**
 * A wrapper class for all media that will be placed on the screen.
 * Subject to change.
 */
public class MediaContainer extends Sprite
{
    /** A ValueEvent we dispatch when our size is known.
     * Value: [ width, height ]. */
    public static const SIZE_KNOWN :String = "mediaSizeKnown";

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
        if (Util.equals(_url, url)) {
            return; // no change
        }
        _url = url;

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
        _url = null;
        if (_media != null) {
            shutdown(false);
        }

        addChild(disp);
        _media = disp;
        updateContentDimensions(disp.width, disp.height);
    }

    /**
     * Configure this sprite to show a video.
     */
    protected function setupVideo (url :String) :void
    {
        var vid :VideoDisplayer = new VideoDisplayer();
        vid.addEventListener(VideoDisplayer.SIZE_KNOWN, handleVideoSizeKnown);
        vid.addEventListener(VideoDisplayer.VIDEO_ERROR, handleVideoError);
        _media = vid;
        addChild(vid);
        vid.setup(url);
    }

    /**
     * Configure this sprite to show an image or flash movie.
     */
    protected function setupSwfOrImage (url :String) :void
    {
        startedLoading();

        // create our loader and set up some event listeners
        var loader :Loader = new Loader();
        _media = loader;
        var info :LoaderInfo = loader.contentLoaderInfo;
        info.addEventListener(Event.COMPLETE, loadingComplete);
        info.addEventListener(IOErrorEvent.IO_ERROR, loadError);
        info.addEventListener(ProgressEvent.PROGRESS, loadProgress);

        // create a mask to prevent the media from drawing out of bounds
        if (getMaxContentWidth() < int.MAX_VALUE &&
                getMaxContentHeight() < int.MAX_VALUE) {
            configureMask(getMaxContentWidth(), getMaxContentHeight());
        }

        // start it loading, add it as a child
        loader.load(new URLRequest(url), getContext(url));
        addChild(loader);

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
        setMediaObject(ImageUtil.createErrorImage(w, h));
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
                removeChild(_media.mask);
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

                removeChild(loader);

            } else if (_media is VideoDisplayer) {
                var vid :VideoDisplayer = (_media as VideoDisplayer);
                vid.removeEventListener(VideoDisplayer.SIZE_KNOWN, handleVideoSizeKnown);
                vid.removeEventListener(VideoDisplayer.VIDEO_ERROR, handleVideoError);
                vid.shutdown();
                removeChild(vid);

            } else if (_media != null) {
                removeChild(_media);
            }
        } catch (ioe :IOError) {
            log.warning("Error shutting down media: " + ioe);
            log.logStackTrace(ioe);
        }

        // clean everything up
        _w = 0;
        _h = 0;
        _media = null;
    }

    /**
     * Get the width of the content, bounded by the maximum.
     */
    public function getContentWidth () :int
    {
        return Math.min(Math.abs(_w * getMediaScaleX()), getMaxContentWidth());
    }

    /**
     * Get the height of the content, bounded by the maximum.
     */
    public function getContentHeight () :int
    {
        return Math.min(Math.abs(_h * getMediaScaleY()), getMaxContentHeight());
    }

    /**
     * Get the maximum allowable width for our content.
     */
    public function getMaxContentWidth () :int
    {
        return int.MAX_VALUE;
    }

    /**
     * Get the maximum allowable height for our content.
     */
    public function getMaxContentHeight () :int
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
     * Called by MediaWrapper as notification that its size has changed.
     */
    public function containerDimensionsUpdated (newWidth :Number, newHeight :Number) :void
    {
        // do nothing in base MediaContainer 
    }

    override public function toString () :String
    {
        return "MediaContainer[url=" + _url + "]";
    }

    /**
     * Return the LoaderContext that should be used to load the media
     * at the specified url.
     */
    protected function getContext (url :String) :LoaderContext
    {
        if (isImage(url)) {
            // load images into our domain so that we can view their pixels
            return new LoaderContext(true, 
                new ApplicationDomain(ApplicationDomain.currentDomain),
                SecurityDomain.currentDomain);

        } else {
            // share nothing, trust nothing
            return new LoaderContext(false, new ApplicationDomain(null), null);
        }
    }

    /**
     * Does the specified url represent an image?
     */
    protected function isImage (url :String) :Boolean
    {
        // look at the last 4 characters in the lowercased url
        switch (url.toLowerCase().slice(-4)) {
        case ".png":
        case ".jpg":
        case ".gif":
            return true;

        default:
            return false;
        }
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
        stoppedLoading();
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
     * Handles video size known.
     */
    protected function handleVideoSizeKnown (event :ValueEvent) :void
    {
        var args :Array = (event.value as Array);
        updateContentDimensions(int(args[0]), int(args[1]));
        trace("Video size is now known: " + _w + ", " + _h);
    }

    /**
     * Handles an error loading a video.
     */
    protected function handleVideoError (event :ValueEvent) :void
    {
        trace("Video load error: " + event.value);
        stoppedLoading();
        setupBrokenImage(-1, -1);
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
        stoppedLoading();
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
            addChild(mask);
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
        // update our saved size, and possibly notify our container
        if (_w != ww || _h != hh) {
            _w = ww;
            _h = hh;
            contentDimensionsUpdated();
            // dispatch an event to any listeners
            dispatchEvent(new ValueEvent(SIZE_KNOWN, [ ww, hh ]));
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

    /**
     * Called when we've started loading new media. Will not be called
     * for new media that does not require loading.
     */
    protected function startedLoading () :void
    {
        // nada
    }

    /**
     * Called when we've stopped loading, which may be as a result of
     * completion, an error while loading, or early termination.
     */
    protected function stoppedLoading () :void
    {
        // nada
    }

    /** The unaltered URL of the content we're displaying. */
    protected var _url :String;

    /** The unscaled width of our content. */
    protected var _w :int;

    /** The unscaled height of our content. */
    protected var _h :int;

    /** Either a Loader or a VideoDisplay. */
    protected var _media :DisplayObject;
}
}
