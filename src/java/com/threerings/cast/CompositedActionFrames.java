//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.cast;

import com.samskivert.util.LRUHashMap;
import com.samskivert.util.StringUtil;

import com.threerings.media.image.Colorization;
import com.threerings.media.image.ImageManager;
import com.threerings.media.util.MultiFrameImage;

import com.threerings.cast.CharacterComponent;
import com.threerings.util.DirectionCodes;

/**
 * An implementation of the {@link MultiFrameImage} interface that is used
 * to lazily create composited character frames when they are requested.
 */
public class CompositedActionFrames
    implements ActionFrames, DirectionCodes
{
    /** Used to associate a {@link CharacterComponent} with its {@link
     * ActionFrames} for a particular action. */
    public static class ComponentFrames
    {
        public CharacterComponent ccomp;

        public ActionFrames frames;

        public String toString () {
            return ccomp + ":" + frames;
        }
    }

    /**
     * Constructs a set of composited action frames with the supplied
     * source frames and colorization configuration. The actual component
     * frame images will not be composited until they are requested.
     */
    public CompositedActionFrames (ImageManager imgr, LRUHashMap frameCache,
                                   String action, ComponentFrames[] sources)
    {
        // sanity check
        if (sources == null || sources.length == 0) {
            String errmsg = "Requested to composite invalid set of source " +
                "frames! [sources=" + StringUtil.toString(sources) + "].";
            throw new RuntimeException(errmsg);
        }

        _imgr = imgr;
        _frameCache = frameCache;
        _sources = sources;
        _action = action;

        // the sources must all have the same orientation count, so we
        // just use the first
        _orientCount = _sources[0].frames.getOrientationCount();
    }

    // documentation inherited from interface
    public int getOrientationCount ()
    {
        return _orientCount;
    }

    // documentation inherited from interface
    public TrimmedMultiFrameImage getFrames (int orient)
    {
        _key.setOrient(orient);
        CompositedMultiFrameImage cmfi =
            (CompositedMultiFrameImage)_frameCache.get(_key);
        if (cmfi == null) {
            cmfi = createFrames(orient);
            _frameCache.put(new CompositedFramesKey(orient), cmfi);
        }
        return cmfi;
    }

    // documentation inherited from interface
    public int getXOrigin (int orient, int frameIdx)
    {
        CompositedMultiFrameImage cmfi = (CompositedMultiFrameImage)
            getFrames(orient);
        return cmfi.getXOrigin(frameIdx);
    }

    // documentation inherited from interface
    public int getYOrigin (int orient, int frameIdx)
    {
        CompositedMultiFrameImage cmfi = (CompositedMultiFrameImage)
            getFrames(orient);
        return cmfi.getYOrigin(frameIdx);
    }

    // documentation inherited from interface
    public ActionFrames cloneColorized (Colorization[] zations)
    {
        throw new RuntimeException("What you talkin' about Willis?");
    }

    /**
     * Creates our underlying multi-frame image for a particular orientation.
     */
    protected CompositedMultiFrameImage createFrames (int orient)
    {
        return new CompositedMultiFrameImage(_imgr, _sources, _action, orient);
    }

    /** Used to cache composited frames for a particular action and
     * orientation. */
    protected class CompositedFramesKey
    {
        public CompositedFramesKey (int orient) {
            _orient = orient;
        }

        public void setOrient (int orient) {
            _orient = orient;
        }

        public CompositedActionFrames getOwner () {
            return CompositedActionFrames.this;
        }

        public boolean equals (Object other) {
            CompositedFramesKey okey = (CompositedFramesKey)other;
            return ((getOwner() == okey.getOwner()) &&
                    (_orient == okey._orient));
        }

        public int hashCode () {
            return CompositedActionFrames.this.hashCode() ^ _orient;
        }

        protected int _orient;
    }

    /** The image manager from whom we can obtain prepared volatile images
     * onto which to render our composited actions. */
    protected ImageManager _imgr;

    /** Used to cache our composited action frame images. */
    protected LRUHashMap _frameCache;

    /** The action for which we're compositing frames. */
    protected String _action;

    /** The number of orientations. */
    protected int _orientCount;

    /** Our source components and action frames. */
    protected ComponentFrames[] _sources;

    /** Used to avoid creating a new key object every time we do a cache
     * lookup. */
    protected CompositedFramesKey _key = new CompositedFramesKey(0);
}
