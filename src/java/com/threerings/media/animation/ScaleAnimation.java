//
// $Id: ScaleAnimation.java 3123 2004-09-18 22:58:39Z mdb $

package com.threerings.media.animation;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;

import com.threerings.media.image.Mirage;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.sprite.SpriteManager;
import com.threerings.media.util.LinearTimeFunction;
import com.threerings.media.util.TimeFunction;

/**
 * Animates an image changing size about its center point.
 */
public class ScaleAnimation extends Animation
{
    /**
     * Creates a scale animation with the supplied image.  If the image's
     * size would ever be 0 or less, it is not drawn.
     *
     * @param image The image to paint.
     *
     * @param center The screen coordinates of the pixel upon which the
     * image's center should always be rendered.
     *
     * @param startScale The amount to scale the image when it is rendered
     * at time 0.
     *
     * @param endScale The amount to scale the image at the final frame
     * of animation.
     *
     * @param duration The time in milliseconds the anim takes to complete.
     */
    public ScaleAnimation (Mirage image, Point center,
                           float startScale, float endScale, int duration)
    {
        super(getBounds(image, center, Math.max(startScale, endScale)));

        // Save inputted variables
        _image = image;
        _center = new Point(center);
        _startScale = startScale;
        _endScale = endScale;
        _duration = duration;

        // Hack the LinearTimeFunction to use fixed point rationals
        //
        // FIXME: This class doesn't seem to be saving me a lot of
        // work, since I have to repackage the outputs into floats
        // anyway.  Find some way to make the LinearTimeFunction do
        // more of this work for us, or write a new class that does.
        // Maybe IntLinearTimeFunction and FloatLinearTimeFunction
        // classes would be useful.
        _scaleFunc = new LinearTimeFunction(0, 10000, duration);
    }

    /**
     * Java wants the first call in a constructor to be super()
     * if it exists at all, so we have to trick it with this function.
     *
     * Oh, and this function computes how big the bounding box needs
     * to be.
     */
    public static Rectangle getBounds (Mirage image, Point center, float scale)
    {
        int width  = (int) (image.getWidth() * scale);
        int height = (int) (image.getWidth() * scale);

        return new Rectangle(center.x - width/2, center.y - height/2,
                             width, height);
    }

    // documentation inherited
    public void tick (long tickStamp)
    {
        // Compute the new scaling value
        float weight = _scaleFunc.getValue(tickStamp) / 10000.0f;
        float scale = ((1.0f - weight) * _startScale) +
                      ((       weight) *   _endScale);

        // Update the animation if the scaling changes
        if (_scale != scale)
        {
            _scale = scale;
            invalidate();
        }
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        _scaleFunc.fastForward(timeDelta);
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        //XXX Write this
        /*
        // TODO: recreate our off image if the sprite bounds changed; we
        // also need to change the bounds of our animation which might
        // require some jockeying (especially if we shrink)
        if (_offimg == null) {
            _offimg = gfx.getDeviceConfiguration().createCompatibleImage(
                _bounds.width, _bounds.height, Transparency.TRANSLUCENT);
        }

        // create a mask image with our sprite and the appropriate color
        Graphics2D ogfx = (Graphics2D)_offimg.getGraphics();
        try {
            ogfx.setColor(_color);
            ogfx.fillRect(0, 0, _bounds.width, _bounds.height);
            ogfx.setComposite(AlphaComposite.DstAtop);
            ogfx.translate(-_sprite.getX(), -_sprite.getY());
            _sprite.paint(ogfx);
        } finally {
            ogfx.dispose();
        }

        Composite ocomp = null;
        Composite ncomp = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, _alpha/1000f);

        // if we're fading the sprite in on the way up, set our alpha
        // composite before we render the sprite
        if (_fadeIn && _upfunc != null) {
            ocomp = gfx.getComposite();
            gfx.setComposite(ncomp);
        }

        // next render the sprite
        _sprite.paint(gfx);

        // if we're not fading in, we still need to alpha the white bits
        if (ocomp == null) {
            ocomp = gfx.getComposite();
            gfx.setComposite(ncomp);
        }

        // now alpha composite our mask atop the sprite
        gfx.drawImage(_offimg, _sprite.getX(), _sprite.getY(), null);
        gfx.setComposite(ocomp);
        */
    }

    /** The image to scale. */
    protected Mirage _image;

    /** The center pixel to render the image around. */
    protected Point _center;

    /** The amount of time the animation should last. */
    //XXX Is this needed?
    protected long _duration;

    /** The amount to scale the image at the start of the animation. */
    protected float _startScale;

    /** The amount to scale the image at the end of the animation. */
    protected float _endScale;

    /** The current amount of scaling to render. */
    protected float _scale;

    /** Computes the image scaling to use at the specified time. */
    protected TimeFunction _scaleFunc;
}
