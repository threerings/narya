//
// $Id: CompositedActionFrames.java,v 1.1 2002/05/04 06:57:24 mdb Exp $

package com.threerings.cast;

import java.awt.Image;
import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.cast.util.TileUtil;

/**
 * An implementation of the {@link MultiFrameImage} interface that is used
 * to lazily create composited character frames when they are requested.
 */
public class CompositedFrameImage implements MultiFrameImage
{
    /**
     * Constructs a composited frame image with the supplied source
     * component images and colorization configuration. The actual
     * component frame images will not be composited until they are
     * requested.
     */
    public CompositedFrameImage (
        String action, MultiFrameImage[] sources, Colorization[][] zations)
    {
        _action = action;
        _sources = sources;
        _zations = zations;

        // the sources must all have the same frame count, so we just use
        // the count from the first one
        int frameCount = _sources[0].getFrameCount();
        _images = new Image[frameCount];
    }

    // documentation inherited
    public int getFrameCount ()
    {
        return _images.length;
    }

    // documentation inherited
    public Image getFrame (int index)
    {
        // create the frame if we don't already have it
        if (_images[index] == null) {
            Log.info("Compositing [action=" + _action +
                     ", index=" + index + "].");
            _images[index] =
                TileUtil.compositeFrames(index, _sources, _zations);
        }                
        return _images[index];
    }

    protected String _action;

    /** Our source frame images. */
    protected MultiFrameImage[] _sources;

    /** The colorizations for our source components. */
    protected Colorization[][] _zations;

    /** The frame images. */
    protected Image[] _images;
}
