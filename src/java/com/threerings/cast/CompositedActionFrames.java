//
// $Id: CompositedActionFrames.java,v 1.2 2002/05/04 19:38:13 mdb Exp $

package com.threerings.cast;

import java.awt.Graphics;
import java.awt.Image;

import com.threerings.util.DirectionCodes;
import com.threerings.media.util.ImageUtil;
import com.threerings.cast.util.TileUtil;

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
    public CompositedActionFrames (
        String action, ActionFrames[] sources, Colorization[][] zations)
    {
        _action = action;
        _sources = sources;
        _zations = zations;

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
    public void paintColoredFrame (
        Graphics g, int index, int x, int y, Colorization[] zations)
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
//             Log.info("Compositing [action=" + _action +
//                      ", orient=" + _orient + ", index=" + index + "].");
            _images[_orient][index] =
                TileUtil.compositeFrames(_orient, index, _sources, _zations);
        }
        return _images[_orient][index];
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

    protected String _action;

    /** The orientation we're currently using. */
    protected int _orient;

    /** The number of frames in each orientation. */
    protected int _frameCount;

    /** Our source action frames. */
    protected ActionFrames[] _sources;

    /** The colorizations for our source components. */
    protected Colorization[][] _zations;

    /** The frame images. */
    protected Image[][] _images;
}
