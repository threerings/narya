//
// $Id: CompositedActionFrames.java,v 1.3 2002/05/06 18:08:31 mdb Exp $

package com.threerings.cast;

import java.awt.Graphics;
import java.awt.Image;

import com.threerings.util.DirectionCodes;
import com.threerings.media.util.Colorization;
import com.threerings.media.util.ImageUtil;

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
        _frameCount = _sources[0].getFrameCount();
        _images = new Image[DIRECTION_COUNT][_frameCount];
    }

    // documentation inherited
    public int getFrameCount ()
    {
        return _frameCount;
    }

    // documentation inherited from interface
    public int getWidth (int index)
    {
        return getExampleSource().getWidth(index);
    }

    // documentation inherited from interface
    public int getHeight (int index)
    {
        return getExampleSource().getHeight(index);
    }

    // documentation inherited from interface
    public void paintFrame (Graphics g, int index, int x, int y)
    {
        g.drawImage(getFrame(index), x, y, null);
    }

    // documentation inherited from interface
    public boolean hitTest (int index, int x, int y)
    {
        return ImageUtil.hitTest(getFrame(index), x, y);
    }

    // documentation inherited from interface
    public void setOrientation (int orient)
    {
        _orient = orient;
    }

    // documentation inherited from interface
    public ActionFrames cloneColorized (Colorization[] zations)
    {
        throw new RuntimeException("What you talkin' about Willis?");
    }

    // documentation inherited
    protected Image getFrame (int index)
    {
        // create the frame if we don't already have it
        if (_images[_orient] == null) {
            _images[_orient] = new Image[_frameCount];
        }
        if (_images[_orient][index] == null) {
//             Log.info("Compositing [orient=" + _orient +
//                      ", index=" + index + "].");
            _images[_orient][index] = compositeFrames(_orient, index);
        }
        return _images[_orient][index];
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
            // create the image now that we know how big it should be
            if (dest == null) {
                dest = ImageUtil.createImage(_sources[ii].getWidth(index),
                                             _sources[ii].getHeight(index));
                g = dest.getGraphics();
            }

            // make sure the action frames are configured with the right
            // orientation before we render
            _sources[ii].setOrientation(orient);

            // render the particular action from this component into the
            // target image
            _sources[ii].paintFrame(g, index, 0, 0);
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

    /**
     * When fetching width and height, we can simply use the first
     * component's image set since they must all be the same.
     */
    protected ActionFrames getExampleSource ()
    {
        ActionFrames source = _sources[0];
        source.setOrientation(_orient);
        return source;
    }

    /** The orientation we're currently using. */
    protected int _orient;

    /** The number of frames in each orientation. */
    protected int _frameCount;

    /** Our source action frames. */
    protected ActionFrames[] _sources;

    /** The frame images. */
    protected Image[][] _images;
}
