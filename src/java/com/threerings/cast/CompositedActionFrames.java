//
// $Id: CompositedActionFrames.java,v 1.4 2002/05/15 23:54:04 mdb Exp $

package com.threerings.cast;

import java.awt.Graphics;
import java.awt.Image;

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
        _sources = sources;
        // the sources must all have the same frame count, so we just use
        // the count from the first one
        _frameCount = _sources[0].getFrames(NORTH).getFrameCount();
        _images = new Image[DIRECTION_COUNT][_frameCount];
    }

    // documentation inherited from interface
    public MultiFrameImage getFrames (final int orient)
    {
        return new MultiFrameImage() {
            // documentation inherited
            public int getFrameCount ()
            {
                return _proto.getFrameCount();
            }

            // documentation inherited from interface
            public int getWidth (int index)
            {
                return _proto.getWidth(index);
            }

            // documentation inherited from interface
            public int getHeight (int index)
            {
                return _proto.getHeight(index);
            }

            // documentation inherited from interface
            public void paintFrame (Graphics g, int index, int x, int y)
            {
                g.drawImage(getFrame(orient, index), x, y, null);
            }

            // documentation inherited from interface
            public boolean hitTest (int index, int x, int y)
            {
                return ImageUtil.hitTest(getFrame(orient, index), x, y);
            }

            protected MultiFrameImage _proto = _sources[0].getFrames(orient);
        };
    }

    // documentation inherited from interface
    public ActionFrames cloneColorized (Colorization[] zations)
    {
        throw new RuntimeException("What you talkin' about Willis?");
    }

    // documentation inherited
    protected Image getFrame (int orient, int index)
    {
        // create the frame if we don't already have it
        if (_images[orient] == null) {
            _images[orient] = new Image[_frameCount];
        }
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
        Image dest = null;
        Graphics g = null;
//         long start = System.currentTimeMillis();

        for (int ii = 0; ii < _sources.length; ii++) {
            MultiFrameImage source = _sources[ii].getFrames(orient);
            // create the image now that we know how big it should be
            if (dest == null) {
                dest = ImageUtil.createImage(source.getWidth(index),
                                             source.getHeight(index));
                g = dest.getGraphics();
            }

            // render the particular action from this component into the
            // target image
            source.paintFrame(g, index, 0, 0);
        }

        // clean up after ourselves
        if (g != null) {
            g.dispose();
        }

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
}
