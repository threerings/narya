//
// $Id: CompositedActionFrames.java,v 1.6 2002/06/20 18:31:03 mdb Exp $

package com.threerings.cast;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import com.samskivert.util.StringUtil;

import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.media.util.Colorization;
import com.threerings.media.util.ImageUtil;

import com.threerings.util.DirectionCodes;

/**
 * An implementation of the {@link MultiFrameImage} interface that is used
 * to lazily create composited character frames when they are requested.
 */
public class CompositedActionFrames
    implements ActionFrames, DirectionCodes
{
    /**
     * Constructs a set of composited action frames with the supplied
     * source frames and colorization configuration. The actual component
     * frame images will not be composited until they are requested.
     */
    public CompositedActionFrames (ActionFrames[] sources)
    {
        // sanity check
        if (sources == null || sources.length == 0) {
            String errmsg = "Requested to composite invalid set of source " +
                "frames! [sources=" + StringUtil.toString(sources) + "].";
            throw new RuntimeException(errmsg);
        }
        _sources = sources;

        // the sources must all have the same frame count, and each
        // orientation must also have the same frame count, so we just use
        // the count from the first source and first orientation
        _frameCount = _sources[0].getFrames(NORTH).getFrameCount();
        _images = new Image[DIRECTION_COUNT][_frameCount];
        _bounds = new Rectangle[DIRECTION_COUNT][_frameCount];
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
                if (_bounds[orient][index] == null) {
                    getFrame(orient, index);
                }
                return _bounds[orient][index].width;
            }

            // documentation inherited from interface
            public int getHeight (int index) {
                // composite the frame if necessary
                if (_bounds[orient][index] == null) {
                    getFrame(orient, index);
                }
                return _bounds[orient][index].height;
            }

            // documentation inherited from interface
            public void paintFrame (Graphics g, int index, int x, int y) {
                g.drawImage(getFrame(orient, index), x, y, null);
            }

            // documentation inherited from interface
            public boolean hitTest (int index, int x, int y) {
                return ImageUtil.hitTest(getFrame(orient, index), x, y);
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
        return _bounds[orient][frameIdx].x;
    }

    // documentation inherited from interface
    public int getYOrigin (int orient, int frameIdx)
    {
        return _bounds[orient][frameIdx].y;
    }

    // documentation inherited from interface
    public ActionFrames cloneColorized (Colorization[] zations)
    {
        throw new RuntimeException("What you talkin' about Willis?");
    }

    // documentation inherited
    protected Image getFrame (int orient, int index)
    {
        // create the arrays for this orientation if we haven't yet
        if (_images[orient] == null) {
            _images[orient] = new Image[_frameCount];
            _bounds[orient] = new Rectangle[_frameCount];
        }

        // create the frame if we don't already have it
        if (_images[orient][index] == null) {
//             Log.info("Compositing [orient=" + orient +
//                      ", index=" + index + "].");
            _images[orient][index] = compositeFrames(orient, index);
        }

        return _images[orient][index];
    }

    /**
     * Renders and returns the <code>index</code>th image from each of the
     * supplied source multi-frame images to a newly created buffered
     * image. This is used to render a single frame of a composited
     * character action, and accordingly, the source image array should be
     * already sorted into the proper rendering order.
     */
    protected Image compositeFrames (int orient, int index)
    {
        int scount = _sources.length;
//         long start = System.currentTimeMillis();

//         // DEBUG
//         int width = 0, height = 0;

        // first we need to determine the bounds of the rectangle that
        // will enclose all of our various components
        Rectangle tbounds = new Rectangle();
        Rectangle bounds = _bounds[orient][index] = new Rectangle(0, 0, 0, 0);
        for (int ii = 0; ii < scount; ii++) {
            TrimmedMultiFrameImage source = _sources[ii].getFrames(orient);
            source.getTrimmedBounds(index, tbounds);
            // the first one defines our initial bounds
            if (bounds.width == 0 && bounds.height == 0) {
                bounds.setBounds(tbounds);
            } else {
                bounds.add(tbounds);
            }

//             // DEBUG
//             for (int ff = 0; ff < _frameCount; ff++) {
//                 width = Math.max(width, source.getWidth(ff));
//                 height = Math.max(height, source.getHeight(ff));
//             }
        }

        // create the image now that we know how big it should be
        Image dest = ImageUtil.createImage(bounds.width, bounds.height);
        Graphics g = dest.getGraphics();

        // now render each of the components into a composited frame
        for (int ii = 0; ii < scount; ii++) {
            TrimmedMultiFrameImage source = _sources[ii].getFrames(orient);
            source.getTrimmedBounds(index, tbounds);

            // render this frame for this particular action for this
            // component into the target image
            source.paintFrame(g, index, -bounds.x, -bounds.y);
        }

        // clean up after ourselves
        if (g != null) {
            g.dispose();
        }

//         Log.info("Composited [orient=" + orient + ", index=" + index +
//                  ", tbounds=" + StringUtil.toString(bounds) +
//                  ", width=" + width + ", height=" + height + "].");

        // keep track of our new origin
        bounds.x = (_sources[0].getXOrigin(orient, index) - bounds.x);
        bounds.y = (_sources[0].getYOrigin(orient, index) - bounds.y);

//         Log.info("New origin [x=" + bounds.x + ", y=" + bounds.y + "].");

//         long now = System.currentTimeMillis();
//         Log.info("Composited " + sources.length + " frames in " +
//                  (now-start) + " millis.");

        return dest;
    }

    /** The number of frames in each orientation. */
    protected int _frameCount;

    /** Our source action frames. */
    protected ActionFrames[] _sources;

    /** The frame images. */
    protected Image[][] _images;

    /** Used to track our trimmed frame bounds. */
    protected Rectangle[][] _bounds;
}
