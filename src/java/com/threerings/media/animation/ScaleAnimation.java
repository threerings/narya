//
// $Id: ScaleAnimation.java 3123 2004-09-18 22:58:39Z mdb $

package com.threerings.media.animation;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.sprite.SpriteManager;
import com.threerings.media.util.LinearTimeFunction;
import com.threerings.media.util.TimeFunction;

/**
 * Washes all non-transparent pixels in a sprite with a particular color
 * (by compositing them with the solid color with progressively higher
 * alpha values) and then back again.
 */
public class ScaleAnimation extends Animation
{
    /**
     * Creates a gleam animation with the supplied sprite. The sprite will
     * be faded to the specified color and then back again. The sprite may
     * be already added to the supplied sprite manager or not, but when
     * the animation is complete, it will have been added.
     *
     * @param fadeIn if true, the sprite itself will be faded in as we
     * fade up to the gleam color and the gleam color will fade out,
     * leaving just the sprite imagery.
     */
    public ScaleAnimation (SpriteManager spmgr, Sprite sprite, Color color,
                           int upmillis, int downmillis, boolean fadeIn)
    {
        super(sprite.getBounds());
        _spmgr = spmgr;
        _sprite = sprite;
        _color = color;
        _upfunc = new LinearTimeFunction(0, 750, upmillis);
        _downfunc = new LinearTimeFunction(750, 0, downmillis);
        _fadeIn = fadeIn;
    }

    // documentation inherited
    public void tick (long timestamp)
    {
        int alpha;
        if (_upfunc != null) {
            if ((alpha = _upfunc.getValue(timestamp)) == 750) {
                _upfunc = null;
            }
        } else if (_downfunc != null) {
            if ((alpha = _downfunc.getValue(timestamp)) == 0) {
                _downfunc = null;
            }
        } else {
            _finished = true;
            _spmgr.addSprite(_sprite);
            return;
        }

        if (_alpha != alpha) {
            _alpha = alpha;
            invalidate();
        }
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        if (_upfunc != null) {
            _upfunc.fastForward(timeDelta);
        } else if (_downfunc != null) {
            _downfunc.fastForward(timeDelta);
        }
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
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
    }

    // documentation inherited
    protected void willStart (long tickStamp)
    {
        super.willStart(tickStamp);

        // remove the sprite we're fiddling with from the manager; we'll
        // add it back when we're done
        if (_spmgr.isManaged(_sprite)) {
            _spmgr.removeSprite(_sprite);
        }
    }

    protected SpriteManager _spmgr;
    protected Sprite _sprite;
    protected Color _color;
    protected Image _offimg;
    protected boolean _fadeIn;

    protected TimeFunction _upfunc;
    protected TimeFunction _downfunc;
    protected int _alpha = -1;
}
