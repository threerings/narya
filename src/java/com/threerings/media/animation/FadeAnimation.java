//
// $Id: FadeAnimation.java,v 1.1 2002/01/11 16:17:33 shaper Exp $

package com.threerings.media.animation;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import com.threerings.media.Log;

/**
 * An animation that displays an image fading from invisibility to full
 * opacity, or vice-versa.  The animation is finished when the target
 * opacity is reached.
 */
public class FadeAnimation extends Animation
{
    /**
     * Constructs a fade animation.
     *
     * @param image the image to animate.
     * @param x the image x-position.
     * @param y the image y-position.
     * @param alpha the starting alpha.
     * @param step the alpha amount to step by each millisecond.
     * @param target the target alpha level.
     */
    public FadeAnimation (
        Image image, int x, int y, float alpha, float step, float target)
    {
        super(new Rectangle(x, y, image.getWidth(null), image.getHeight(null)));

        // save things off
        _image = image;
        _x = x;
        _y = y;
        _startAlpha = _alpha = alpha;
        _step = step;
        _target = target;

        // create the initial composite
        _comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha);

        // save the animation starting time
        _start = System.currentTimeMillis();
    }

    // documentation inherited
    public void tick (long timestamp)
    {
        // figure out the current alpha
        long msecs = timestamp - _start;
        _alpha = Math.min(_startAlpha + (msecs * _step), 1.0f);
        _comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha);

        // check whether we're done and dirty ourselves
        _finished = ((_startAlpha < _target) ? (_alpha >= _target) :
                     (_alpha <= _target));
        invalidate();

        // Log.info("Ticked fade [anim=" + this + "].");
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        Composite ocomp = gfx.getComposite();
        if (_comp == null) {
            Log.warning("Fade anim has null composite [anim=" + this + "].");
        } else {
            gfx.setComposite(_comp);
        }
        gfx.drawImage(_image, _x, _y, null);
        gfx.setComposite(ocomp);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);

        buf.append(", x=").append(_x);
        buf.append(", y=").append(_y);
        buf.append(", alpha=").append(_alpha);
        buf.append(", startAlpha=").append(_startAlpha);
        buf.append(", step=").append(_step);
        buf.append(", target=").append(_target);
    }

    /** The composite object used to render the image with desired opacity. */
    protected Composite _comp;

    /** The current alpha of the image. */
    protected float _alpha;

    /** The target alpha. */
    protected float _target;

    /** The alpha step per millisecond. */
    protected float _step;

    /** The starting alpha. */
    protected float _startAlpha;

    /** The image position. */
    protected int _x, _y;

    /** The image to animate. */
    protected Image _image;

    /** The starting animation time. */
    protected long _start;
}
