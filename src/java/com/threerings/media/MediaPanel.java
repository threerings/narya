//
// $Id: MediaPanel.java,v 1.30 2003/03/25 23:05:58 mdb Exp $

package com.threerings.media;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;

import java.util.Arrays;

import com.samskivert.swing.event.AncestorAdapter;
import com.samskivert.util.IntListUtil;
import com.samskivert.util.StringUtil;

import com.threerings.media.animation.Animation;
import com.threerings.media.animation.AnimationManager;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.sprite.SpriteManager;

/**
 * Provides a useful extensible framework for rendering animated displays
 * that use sprites and animations. Sprites and animations can be added to
 * this panel and they will automatically be ticked and rendered (see
 * {@link #addSprite} and {@link #addAnimation}).
 *
 * <p> To facilitate optimized sprite and animation rendering, the panel
 * provides a dirty region manager which is used to only repaint dirtied
 * regions on each frame. Derived classes can add dirty regions to the
 * scene and/or augment the dirty regions created by moving sprites and
 * changing animations.
 *
 * <p> Sprite and animation rendering is done in two layers: front and
 * back. Callbacks are provided to render behind the back layer ({@link
 * #paintBehind}), in between front and back ({@link #paintBetween}) and
 * in front of the front layer ({@link #paintInFront}).
 *
 * <p> The animated panel automatically registers with the {@link
 * FrameManager} to participate in the frame tick. It only does so while
 * it is a visible part of the UI hierarchy, so animations and sprites are
 * paused while the animated panel is hidden.
 */
public class MediaPanel extends JComponent
    implements FrameParticipant, MediaConstants,
               FrameManager.PerformanceProvider
{
    /**
     * Constructs a media panel.
     */
    public MediaPanel (FrameManager framemgr)
    {
        // keep this for later
        _framemgr = framemgr;

        // create our region manager
        _remgr = new RegionManager();

        // create our animation and sprite managers
        _animmgr = new AnimationManager(_remgr);
        _spritemgr = new SpriteManager(_remgr);

        // participate in the frame when we're visible
        addAncestorListener(new AncestorAdapter() {
            public void ancestorAdded (AncestorEvent event) {
                _framemgr.registerFrameParticipant(MediaPanel.this);
            }
            public void ancestorRemoved (AncestorEvent event) {
                _framemgr.removeFrameParticipant(MediaPanel.this);
            }
        });
    }

    /**
     * Pauses the sprites and animations that are currently active on this
     * media panel. Also stops listening to the frame tick while paused.
     */
    public void setPaused (boolean paused)
    {
        // sanity check
        if ((paused && (_pauseTime != 0)) ||
            (!paused && (_pauseTime == 0))) {
            Log.warning("Requested to pause when paused or vice-versa " +
                        "[paused=" + paused + "].");
            return;
        }

        if (_paused = paused) {
            // make a note of our pause time
            _pauseTime = _framemgr.getTimeStamp();

        } else {
            // let the animation and sprite managers know that we just
            // warped into the future
            long delta = _framemgr.getTimeStamp() - _pauseTime;
            _animmgr.fastForward(delta);
            _spritemgr.fastForward(delta);

            // clear out our pause time
            _pauseTime = 0;
        }
    }

    /**
     * Returns a reference to the region manager that is used to
     * accumulate dirty regions each frame.
     */
    public RegionManager getRegionManager ()
    {
        return _remgr;
    }

    /**
     * Returns a timestamp from the {@link MediaTimer} used to track time
     * intervals for this media panel. <em>Note:</em> this should only be
     * called from the AWT thread.
     */
    public long getTimeStamp ()
    {
        return _framemgr.getTimeStamp();
    }

    // documentation inherited from interface
    public void getPerformanceStatus (StringBuffer buf)
    {
        buf.append("MP:").append(_dirtyPerTick);
    }

    /**
     * Adds a sprite to this panel.
     */
    public void addSprite (Sprite sprite)
    {
        _spritemgr.addSprite(sprite);
    }

    /**
     * Removes a sprite from this panel.
     */
    public void removeSprite (Sprite sprite)
    {
        _spritemgr.removeSprite(sprite);
    }

    /**
     * Removes all sprites from this panel.
     */
    public void clearSprites ()
    {
        _spritemgr.clearMedia();
    }

    /**
     * Adds an animation to this panel.
     */
    public void addAnimation (Animation anim)
    {
        _animmgr.registerAnimation(anim);
    }

    /**
     * Removes an animation from this panel.
     */
    public void removeAnimation (Animation anim)
    {
        _animmgr.unregisterAnimation(anim);
    }

    /**
     * Removes all animations from this panel.
     */
    public void clearAnimations ()
    {
        _animmgr.clearMedia();
    }

    // documentation inherited from interface
    public void tick (long tickStamp)
    {
        // bail if ticking is currently disabled
        if (_paused) {
            return;
        }

        // let derived classes do their business
        willTick(tickStamp);

        // now tick our animations and sprites
        _animmgr.tick(tickStamp);
        _spritemgr.tick(tickStamp);

        // let derived classes do their business
        didTick(tickStamp);

        // make a note that the next paint will correspond to a call to
        // tick()
        _tickPaintPending = true;
    }

    /**
     * Derived classes can override this method and perform computation
     * prior to the ticking of the sprite and animation managers.
     */
    protected void willTick (long tickStamp)
    {
    }

    /**
     * Derived classes can override this method and perform computation
     * subsequent to the ticking of the sprite and animation managers.
     */
    protected void didTick (long tickStamp)
    {
    }

    // documentation inherited from interface
    public boolean needsPaint ()
    {
        // compute our average dirty regions per tick
        if (_tick++ == 99) {
            _tick = 0;
            int dirty = IntListUtil.sum(_dirty);
            Arrays.fill(_dirty, 0);
            _dirtyPerTick = (float)dirty/100;
        }

        return (!_tickPaintPending || _remgr.haveDirtyRegions());
    }

    // documentation inherited from interface
    public Component getComponent ()
    {
        return this;
    }

    // documentation inherited
    public void repaint (long tm, int x, int y, int width, int height)
    {
        if (width > 0 && height > 0) {
            dirtyScreenRect(new Rectangle(x, y, width, height));
        }
    }

    // documentation inherited
    public void paint (Graphics g)
    {
        Graphics2D gfx = (Graphics2D)g;

        // no use in painting if we're not showing or if we've not yet
        // been validated
        if (!isValid() || !isShowing()) {
            return;
        }

        // if this isn't a tick paint, then we need to grab the clipping
        // rectangle and mark it as dirty
        if (!_tickPaintPending) {
            Shape clip = g.getClip();
            if (clip == null) {
                // mark the whole component as dirty
                repaint();
            } else {
                dirtyScreenRect(clip.getBounds());
            }

            // we used to bail out here and not render until our next
            // tick, but it turns out that we need to render here because
            // Swing may have repainted our parent over us and expect that
            // we're going to paint ourselves on top of whatever it just
            // painted, so we go ahead and paint now to avoid flashing

        } else {
            _tickPaintPending = false;
        }

        // if we have no invalid rects, there's no need to repaint
        if (!_remgr.haveDirtyRegions()) {
            return;
        }

        // get our dirty rectangles and delegate the main painting to a
        // method that can be more easily overridden
        Rectangle[] dirty = _remgr.getDirtyRegions();
        _dirty[_tick] = dirty.length;
        paint(gfx, dirty);
    }

    /**
     * Performs the actual painting of the media panel. Derived methods
     * can override this method if they wish to perform pre- and/or
     * post-paint activities or if they wish to provide their own painting
     * mechanism entirely.
     */
    protected void paint (Graphics2D gfx, Rectangle[] dirty)
    {
        int dcount = dirty.length;

        for (int ii = 0; ii < dcount; ii++) {
            Rectangle clip = dirty[ii];
            // sanity-check the dirty rectangle
            if (clip == null) {
                Log.warning("Found null dirty rect painting media panel?!");
                Thread.dumpStack();
                continue;
            }

            // constrain this dirty region to the bounds of the component
            constrainToBounds(clip);

            // ignore rectangles that were reduced to nothingness
            if (clip.width == 0 || clip.height == 0) {
                continue;
            }

            // clip to this dirty region
            clipToDirtyRegion(gfx, clip);

            // paint the behind the scenes stuff
            paintBehind(gfx, clip);

            // paint back sprites and animations
            paintBits(gfx, AnimationManager.BACK, clip);

            // paint the between the scenes stuff
            paintBetween(gfx, clip);

            // paint front sprites and animations
            paintBits(gfx, AnimationManager.FRONT, clip);

            // paint anything in front
            paintInFront(gfx, clip);
        }
    }

    /**
     * Called by the main rendering code to constrain this dirty rectangle
     * to the bounds of the media panel. If a derived class is using dirty
     * rectangles that live in some sort of virtual coordinate system,
     * they'll want to override this method and constraint the rectangles
     * properly.
     */
    protected void constrainToBounds (Rectangle dirty)
    {
        SwingUtilities.computeIntersection(
            0, 0, getWidth(), getHeight(), dirty);
    }

    /**
     * This is called to clip the rendering output to the supplied dirty
     * region. This should use {@link Graphics#setClip} because the
     * clipping region will need to be replaced as we iterate through our
     * dirty regions. By default, a region is assumed to represent screen
     * coordinates, but if a derived class wishes to maintain dirty
     * regions in non-screen coordinates, it can override this method to
     * properly clip to the dirty region.
     */
    protected void clipToDirtyRegion (Graphics2D gfx, Rectangle dirty)
    {
//         Log.info("MP: Clipping to [clip=" + StringUtil.toString(dirty) + "].");
        gfx.setClip(dirty);
    }

    /**
     * Called to mark the specified rectangle (in screen coordinates) as
     * dirty. The rectangle will be bent, folded and mutilated, so be sure
     * you're not passing a rectangle into this method that is being used
     * elsewhere.
     *
     * <p> If derived classes wish to convert from screen coordinates to
     * some virtual coordinate system to be used by their repaint manager,
     * this is the place to do it.
     */
    protected void dirtyScreenRect (Rectangle rect)
    {
        _remgr.addDirtyRegion(rect);
    }

    /**
     * Paints behind all sprites and animations. The supplied invalid
     * rectangle should be redrawn in the supplied graphics context.
     * Sub-classes should override this method to do the actual rendering
     * for their display.
     */
    protected void paintBehind (Graphics2D gfx, Rectangle dirtyRect)
    {
    }

    /**
     * Paints between the front and back layer of sprites and animations.
     * The supplied invalid rectangle should be redrawn in the supplied
     * graphics context. Sub-classes should override this method to do the
     * actual rendering for their display.
     */
    protected void paintBetween (Graphics2D gfx, Rectangle dirtyRect)
    {
    }

    /**
     * Paints in front of all sprites and animations. The supplied invalid
     * rectangle should be redrawn in the supplied graphics context.
     * Sub-classes should override this method to do the actual rendering
     * for their display.
     */
    protected void paintInFront (Graphics2D gfx, Rectangle dirtyRect)
    {
    }

    /**
     * Renders the sprites and animations that intersect the supplied
     * dirty region in the specified layer. Derived classes can override
     * this method if they need to do custom sprite or animation rendering
     * (if they need to do special sprite z-order handling, for example).
     * The clipping region will already be set appropriately.
     */
    protected void paintBits (Graphics2D gfx, int layer, Rectangle dirty)
    {
        _animmgr.renderMedia(gfx, layer, dirty);
        _spritemgr.renderMedia(gfx, layer, dirty);
    }

    /** The frame manager with whom we register. */
    protected FrameManager _framemgr;

    /** The animation manager in use by this panel. */
    protected AnimationManager _animmgr;

    /** The sprite manager in use by this panel. */
    protected SpriteManager _spritemgr;

    /** Used to accumulate and merge dirty regions on each tick. */
    protected RegionManager _remgr;

    /** Used to correlate tick()s with paint()s. */
    protected boolean _tickPaintPending = false;

    /** Whether we're currently paused. */
    protected boolean _paused;

    /** Used to track the clock time at which we were paused. */
    protected long _pauseTime;

    /** Used to keep metrics. */
    protected int[] _dirty = new int[200];

    /** Used to keep metrics. */
    protected int _tick;

    /** Used to keep metrics. */
    protected float _dirtyPerTick;
}
