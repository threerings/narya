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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;

import java.util.Arrays;
import java.util.Comparator;

import com.threerings.media.image.ImageManager;
import com.threerings.media.image.Mirage;
import com.threerings.media.image.VolatileMirage;

import com.threerings.cast.CompositedActionFrames.ComponentFrames;
import com.threerings.cast.TrimmedMultiFrameImage;

/**
 * Used to composite the action frames for a particular orientation of a
 * {@link CompositedActionFrames}.
 */
public class CompositedMultiFrameImage
    implements TrimmedMultiFrameImage
{
    public CompositedMultiFrameImage (
        ImageManager imgr, ComponentFrames[] sources,
        String action, int orient)
    {
        _imgr = imgr;
        _sources = sources;
        _action = action;
        _orient = orient;

        // create our frame images (which will do the compositing)
        int fcount = sources[0].frames.getFrames(orient).getFrameCount();
        _images = new CompositedMirage[fcount];
        for (int ii = 0; ii < fcount; ii++) {
            _images[ii] = createCompositedMirage(ii);
        }
    }
    
    // documentation inherited
    public int getFrameCount () {
        return _images.length;
    }

    // documentation inherited from interface
    public int getWidth (int index) {
        return _images[index].getWidth();
    }

    // documentation inherited from interface
    public int getHeight (int index) {
        return _images[index].getHeight();
    }

    public int getXOrigin (int index) {
        return _images[index].getXOrigin();
    }

    public int getYOrigin (int index) {
        return _images[index].getYOrigin();
    }

    // documentation inherited from interface
    public void paintFrame (Graphics2D g, int index, int x, int y) {
        _images[index].paint(g, x, y);
    }

    // documentation inherited from interface
    public boolean hitTest (int index, int x, int y) {
        return _images[index].hitTest(x, y);
    }

    // documentation inherited from interface
    public void getTrimmedBounds (int index, Rectangle bounds) {
        bounds.setBounds(0, 0, getWidth(index), getHeight(index));
    }

    /**
     * Returns the estimated memory usage of our composited frame images.
     */
    public long getEstimatedMemoryUsage ()
    {
        long size = 0;
        for (int ii = 0; ii < _images.length; ii++) {
            size += _images[ii].getEstimatedMemoryUsage();
        }
        return size;
    }

    /**
     * Creates a composited image for the specified frame.
     */
    protected CompositedMirage createCompositedMirage (int index)
    {
        return new CompositedMirage(index);
    }
    
    // documentation inherited
    protected Mirage getFrame (int orient, int index)
    {
        return _images[index];
    }

    /** The image manager from whom we load our images. */
    protected ImageManager _imgr;

    /** The action frames from which we obtain our source imagery. */
    protected ComponentFrames[] _sources;

    /** The action we're compositing. */
    protected String _action;

    /** The orientation we're compositing. */
    protected int _orient;

    /** Our composited action frame images. */
    protected CompositedMirage[] _images;

    /**
     * Used to create our mirage using the source action frame images.
     */
    protected class CompositedMirage extends VolatileMirage
        implements Comparator
    {
        public CompositedMirage (int index)
        {
            super(CompositedMultiFrameImage.this._imgr,
                  new Rectangle(0, 0, 0, 0));

            // keep this for later
            _index = index;

            // first we need to determine the bounds of the rectangle that
            // will enclose all of our various components
            Rectangle tbounds = new Rectangle();
            int scount = _sources.length;
            for (int ii = 0; ii < scount; ii++) {
                TrimmedMultiFrameImage source =
                    _sources[ii].frames.getFrames(_orient);
                source.getTrimmedBounds(index, tbounds);
                _bounds = combineBounds(_bounds, tbounds);
            }

            // compute our new origin
            _origin.x = (_sources[0].frames.getXOrigin(_orient, index) -
                         _bounds.x);
            _origin.y = (_sources[0].frames.getYOrigin(_orient, index) -
                         _bounds.y);
//             Log.info("New origin [x=" + _origin.x + ", y=" + _origin.y + "].");

            // render our volatile image for the first time
            createVolatileImage();
        }
        
        public int getXOrigin ()
        {
            return _origin.x;
        }

        public int getYOrigin ()
        {
            return _origin.y;
        }

        // documentation inherited from interface
        public int compare (Object o1, Object o2)
        {
            ComponentFrames cf1 = (ComponentFrames)o1,
                cf2 = (ComponentFrames)o2;
            return (cf1.ccomp.componentClass.getRenderPriority(
                        _action, _orient) -
                    cf2.ccomp.componentClass.getRenderPriority(
                        _action, _orient));
        }

        /**
         * Combines the working bounds with a new set of bounds.
         */
        protected Rectangle combineBounds (Rectangle bounds, Rectangle tbounds)
        {
            // the first one defines our initial bounds
            if (bounds.width == 0 && bounds.height == 0) {
                bounds.setBounds(tbounds);
            } else {
                bounds.add(tbounds);
            }
            return bounds;
        }
        
        // documentation inherited
        protected int getTransparency ()
        {
            return Transparency.BITMASK;
        }

        // documentation inherited
        protected void refreshVolatileImage ()
        {
//             long start = System.currentTimeMillis();

            // sort the sources appropriately for this orientation
            Arrays.sort(_sources, this);

            // now render each of the components into a composited frame
            int scount = _sources.length;
            Graphics2D g = (Graphics2D)_image.getGraphics();
            try {
                for (int ii = 0; ii < scount; ii++) {
                    TrimmedMultiFrameImage source =
                        _sources[ii].frames.getFrames(_orient);
                    source.paintFrame(g, _index, -_bounds.x, -_bounds.y);
                }
            } finally {
                // clean up after ourselves
                if (g != null) {
                    g.dispose();
                }
            }

//             Log.info("Composited [orient=" + _orient + ", index=" + _index +
//                      ", tbounds=" + StringUtil.toString(_bounds) + "].");

//             long now = System.currentTimeMillis();
//             Log.info("Composited " + scount + " frames in " +
//                      (now-start) + " millis.");
        }

        protected int _index;
        protected Point _origin = new Point();
    }
};
