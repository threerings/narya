//
// $Id$

package com.threerings.media.animation;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.threerings.media.image.Mirage;
import com.threerings.media.util.LinearTimeFunction;
import com.threerings.media.util.TimeFunction;

/**
 * Blends between a series of images using alpha.
 */
public class BlendAnimation extends Animation
{
    /**
     * Blends from the starting image through each successive image in the
     * specified amount of time (blending between each image takes place
     * in <code>delay</code> milliseconds).
     */
    public BlendAnimation (int x, int y, Mirage[] images, int delay)
    {
        super(new Rectangle(x, y, images[0].getWidth(), images[0].getHeight()));
        _images = images;
        int fades = images.length-1;
        _tfunc = new LinearTimeFunction(0, 100  * fades, delay * fades);
    }

    // documentation inherited
    public void tick (long timestamp)
    {
        // check to see if our blend level has changed
        int level = _tfunc.getValue(timestamp);
        if (level == _level) {
            return;
        }
        // stop if we reach the end
        if (level == 100*(_images.length-1)) {
            _finished = true;
        }
        _level = level;
        invalidate();
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        _tfunc.fastForward(timeDelta);
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        int index = _level / 100;
        float alpha = 1f - (_level % 100) / 100f;

        Composite ocomp = gfx.getComposite();
        gfx.setComposite(AlphaComposite.getInstance(
                             AlphaComposite.SRC_OVER, alpha));
        _images[index].paint(gfx, _bounds.x, _bounds.y);
        if (index < _images.length-1) {
            gfx.setComposite(AlphaComposite.getInstance(
                                 AlphaComposite.SRC_OVER, 1f-alpha));
            _images[index+1].paint(gfx, _bounds.x, _bounds.y);
        }
        gfx.setComposite(ocomp);
    }

    /** The images between which we are blending. */
    protected Mirage[] _images;

    /** The time function we're using to time our blends. */
    protected TimeFunction _tfunc;

    /** Our current blend level and image index all wrapped into one. */
    protected int _level;

    /** The alpha composite used to render our current image. */
    protected AlphaComposite _currentComp;

    /** The alpha composite used to render our "next" image. */
    protected AlphaComposite _nextComp;
}
