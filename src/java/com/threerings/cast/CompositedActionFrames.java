//
// $Id: CompositedActionFrames.java,v 1.14 2003/01/17 02:28:32 mdb Exp $

package com.threerings.cast;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.util.Arrays;
import java.util.Comparator;

import com.samskivert.util.StringUtil;

import com.threerings.media.image.Colorization;
import com.threerings.media.image.ImageManager;
import com.threerings.media.image.Mirage;
import com.threerings.media.image.VolatileMirage;
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
    public CompositedActionFrames (ImageManager imgr, String action,
                                   ComponentFrames[] sources)
    {
        // sanity check
        if (sources == null || sources.length == 0) {
            String errmsg = "Requested to composite invalid set of source " +
                "frames! [sources=" + StringUtil.toString(sources) + "].";
            throw new RuntimeException(errmsg);
        }
        _imgr = imgr;
        _sources = sources;
        _action = action;

        // the sources must all have the same orientation count, and each
        // orientation must also have the same frame count, so we just use
        // the counts from the first source and orientation
        _orientCount = _sources[0].frames.getOrientationCount();
        _frameCount = _sources[0].frames.getFrames(NORTH).getFrameCount();
        _images = new CompositedMirage[_orientCount][_frameCount];
    }

    // documentation inherited from interface
    public int getOrientationCount ()
    {
        return _orientCount;
    }

    // documentation inherited from interface
    public TrimmedMultiFrameImage getFrames (final int orient)
    {
        return new TrimmedMultiFrameImage() {
            // documentation inherited
            public int getFrameCount () {
                return _frameCount;
            }

            // documentation inherited from interface
            public int getWidth (int index) {
                // composite the frame if necessary
                if (_images[orient][index] == null) {
                    getFrame(orient, index);
                }
                return _images[orient][index].getWidth();
            }

            // documentation inherited from interface
            public int getHeight (int index) {
                // composite the frame if necessary
                if (_images[orient][index] == null) {
                    getFrame(orient, index);
                }
                return _images[orient][index].getHeight();
            }

            // documentation inherited from interface
            public void paintFrame (Graphics2D g, int index, int x, int y) {
                getFrame(orient, index).paint(g, x, y);
            }

            // documentation inherited from interface
            public boolean hitTest (int index, int x, int y) {
                return getFrame(orient, index).hitTest(x, y);
            }

            // documentation inherited from interface
            public void getTrimmedBounds (int index, Rectangle bounds) {
                bounds.setBounds(0, 0, getWidth(index), getHeight(index));
            }
        };
    }

    // documentation inherited from interface
    public int getXOrigin (int orient, int frameIdx)
    {
        return _images[orient][frameIdx].getXOrigin();
    }

    // documentation inherited from interface
    public int getYOrigin (int orient, int frameIdx)
    {
        return _images[orient][frameIdx].getYOrigin();
    }

    // documentation inherited from interface
    public ActionFrames cloneColorized (Colorization[] zations)
    {
        throw new RuntimeException("What you talkin' about Willis?");
    }

    // documentation inherited from interface
    public long getEstimatedMemoryUsage ()
    {
        long size = 0;
        for (int orient = 0; orient < _orientCount; orient++) {
            for (int ii = 0; ii < _images[orient].length; ii++) {
                Mirage mirage = _images[orient][ii];
                if (mirage == null) {
                    continue;
                }
                size += mirage.getEstimatedMemoryUsage();
            }
        }
        return size;
    }

    // documentation inherited
    protected Mirage getFrame (int orient, int index)
    {
        // create the frame if we don't already have it
        if (_images[orient][index] == null) {
//             Log.info("Compositing [action=" + _action + ", orient=" + orient +
//                      ", index=" + index + "].");
            _images[orient][index] = new CompositedMirage(_imgr, orient, index);
        }

        return _images[orient][index];
    }

    /**
     * Used to create our mirage using the source action frame images.
     */
    protected class CompositedMirage extends VolatileMirage
        implements Comparator
    {
        public CompositedMirage (ImageManager imgr, int orient, int index)
        {
            super(imgr, new Rectangle(0, 0, 0, 0));

            // keep these for later
            _orient = orient;
            _index = index;

            // first we need to determine the bounds of the rectangle that
            // will enclose all of our various components
            Rectangle tbounds = new Rectangle();
            int scount = _sources.length;
            for (int ii = 0; ii < scount; ii++) {
                TrimmedMultiFrameImage source =
                    _sources[ii].frames.getFrames(orient);
                source.getTrimmedBounds(index, tbounds);

                // the first one defines our initial bounds
                if (_bounds.width == 0 && _bounds.height == 0) {
                    _bounds.setBounds(tbounds);
                } else {
                    _bounds.add(tbounds);
                }
            }

            // compute our new origin
            _origin.x = (_sources[0].frames.getXOrigin(orient, index) -
                         _bounds.x);
            _origin.y = (_sources[0].frames.getYOrigin(orient, index) -
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
                        _action, _sorient) -
                    cf2.ccomp.componentClass.getRenderPriority(
                        _action, _sorient));
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
            _sorient = _orient;
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

        protected int _orient;
        protected int _index;
        protected Point _origin = new Point();
    }

    /** The image manager from whom we can obtain prepared volatile images
     * onto which to render our composited actions. */
    protected ImageManager _imgr;

    /** The action for which we're compositing frames. */
    protected String _action;

    /** The orientation for which we're currently sorting. */
    protected int _sorient;

    /** The number of orientations. */
    protected int _orientCount;

    /** The number of frames in each orientation. */
    protected int _frameCount;

    /** Our source components and action frames. */
    protected ComponentFrames[] _sources;

    /** The frame images. */
    protected CompositedMirage[][] _images;
}
