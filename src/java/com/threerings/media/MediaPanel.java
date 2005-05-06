//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.media;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.MouseInputAdapter;

import java.util.Arrays;

import com.samskivert.swing.Controller;
import com.samskivert.swing.Label;
import com.samskivert.swing.event.AncestorAdapter;
import com.samskivert.swing.event.CommandEvent;
import com.samskivert.util.IntListUtil;
import com.samskivert.util.RuntimeAdjust;
import com.samskivert.util.StringUtil;

import com.threerings.media.timer.MediaTimer;

import com.threerings.media.animation.Animation;
import com.threerings.media.animation.AnimationManager;

import com.threerings.media.sprite.ButtonSprite;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.sprite.SpriteManager;

import com.threerings.media.sprite.action.ActionSprite;
import com.threerings.media.sprite.action.ArmingSprite;
import com.threerings.media.sprite.action.CommandSprite;
import com.threerings.media.sprite.action.DisableableSprite;
import com.threerings.media.sprite.action.HoverSprite;

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
    implements FrameParticipant, MediaConstants
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
        _animmgr = new AnimationManager(this);
        _spritemgr = new SpriteManager(this);

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
     * Returns a reference to the animation manager used by this media
     * panel.
     */
    public AnimationManager getAnimationManager ()
    {
        return _animmgr;
    }

    /**
     * Returns a reference to the sprite manager used by this media panel.
     */
    public SpriteManager getSpriteManager ()
    {
        return _spritemgr;
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
    }

    /**
     * Adds a sprite to this panel.
     */
    public void addSprite (Sprite sprite)
    {
        _spritemgr.addSprite(sprite);
        
        if (((sprite instanceof ActionSprite) ||
             (sprite instanceof HoverSprite)) && (_actionSpriteCount++ == 0)) {
            if (_actionHandler == null) {
                _actionHandler = new ActionSpriteHandler();
            }
            addMouseListener(_actionHandler);
            addMouseMotionListener(_actionHandler);
        }
    }

    /**
     * @return true if the sprite is already added to this panel.
     */
    public boolean isManaged (Sprite sprite)
    {
        return _spritemgr.isManaged(sprite);
    }

    /**
     * Removes a sprite from this panel.
     */
    public void removeSprite (Sprite sprite)
    {
        _spritemgr.removeSprite(sprite);
        
        if (((sprite instanceof ActionSprite) ||
             (sprite instanceof HoverSprite)) && (--_actionSpriteCount == 0)) {
            removeMouseListener(_actionHandler);
            removeMouseMotionListener(_actionHandler);
        }
    }

    /**
     * Removes all sprites from this panel.
     */
    public void clearSprites ()
    {
        _spritemgr.clearMedia();

        if (_actionHandler != null) {
            removeMouseListener(_actionHandler);
            removeMouseMotionListener(_actionHandler);
            _actionSpriteCount = 0;
        }
    }

    /**
     * Adds an animation to this panel. Animations are automatically
     * removed when they finish.
     */
    public void addAnimation (Animation anim)
    {
        _animmgr.registerAnimation(anim);
    }

    /**
     * @return true if the animation is already added to this panel.
     */
    public boolean isManaged (Animation anim)
    {
        return _animmgr.isManaged(anim);
    }

    /**
     * Aborts a currently running animation and removes it from this
     * panel. Animations are normally automatically removed when they
     * finish.
     */
    public void abortAnimation (Animation anim)
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

        // if performance debugging is enabled, 
        if (_perfDebug.getValue()) {
            if (_perfLabel == null) {
                _perfLabel = new Label(
                    "", Label.OUTLINE, Color.white, Color.black,
                    new Font("Arial", Font.PLAIN, 10));
            }
            if (_perfRect == null) {
                _perfRect = new Rectangle(5, 5, 0, 0);
            }

            StringBuffer perf = new StringBuffer();
            perf.append("[FPS: ");
            perf.append(_framemgr.getPerfTicks()).append("/");
            perf.append(_framemgr.getPerfTries());
            perf.append(" PM:");
            StringUtil.toString(perf, _framemgr.getPerfMetrics());
//             perf.append(" MP:").append(_dirtyPerTick);
            perf.append("]");

            String perfStatus = perf.toString();
            if (!_perfStatus.equals(perfStatus)) {
                _perfStatus = perfStatus;
                _perfLabel.setText(perfStatus);

                Graphics2D gfx = (Graphics2D)getGraphics();
                if (gfx != null) {
                    _perfLabel.layout(gfx);
                    gfx.dispose();

                    // make sure the region we dirty contains the old and
                    // the new text (which we ensure by never letting the
                    // rect shrink)
                    Dimension psize = _perfLabel.getSize();
                    _perfRect.width = Math.max(_perfRect.width, psize.width);
                    _perfRect.height = Math.max(_perfRect.height, psize.height);
                    dirtyScreenRect(new Rectangle(_perfRect));
                }
            }
        }

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

        // if we have no dirty regions, clear our pending tick indicator
        // because we're not going to get painted
        boolean needsPaint = _remgr.haveDirtyRegions();
        if (!needsPaint) {
            _tickPaintPending = false;
        }

        // regardless of whether or not we paint, we need to let our
        // abstract media managers know that we've gotten to the point of
        // painting because they need to remain prepared to deal with
        // media changes that happen any time between the tick() and the
        // paint() and thus need to know when paint() happens
        _animmgr.willPaint();
        _spritemgr.willPaint();

        return needsPaint;
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
        try {
            paint(gfx, dirty);
        } catch (Throwable t) {
            Log.warning(this + " choked in paint(" + dirty + ").");
            Log.logStackTrace(t);
        }

        // render our performance debugging if it's enabled
        if (_perfRect != null && _perfDebug.getValue()) {
            gfx.setClip(null);
            _perfLabel.render(gfx, _perfRect.x, _perfRect.y);
        }
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
        if (layer == FRONT) {
            _spritemgr.paint(gfx, layer, dirty);
            _animmgr.paint(gfx, layer, dirty);
        } else {
            _animmgr.paint(gfx, layer, dirty);
            _spritemgr.paint(gfx, layer, dirty);
        }
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

    /** Handles ActionSprite/HoverSprite/ArmingSprite manipulation. */
    protected class ActionSpriteHandler extends MouseInputAdapter
    {
        // documentation inherited
        public void mousePressed (MouseEvent me)
        {
            if (_activeSprite == null) {
                // see if we can find one
                Sprite s = getHit(me);
                if (s instanceof ActionSprite) {
                    _activeSprite = s;
                }
            }

            if (_activeSprite instanceof ArmingSprite) {
                ((ArmingSprite) _activeSprite).setArmed(true);
            }
        }
        
        // documentation inherited
        public void mouseReleased (MouseEvent me)
        {
            if (_activeSprite instanceof ArmingSprite) {
                ((ArmingSprite)_activeSprite).setArmed(false);
            }
            
            if ((_activeSprite instanceof ActionSprite) &&
                    _activeSprite.hitTest(me.getX(), me.getY())) {
                ActionEvent event;
                if (_activeSprite instanceof CommandSprite) {
                    CommandSprite cs = (CommandSprite) _activeSprite;
                    event = new CommandEvent(
                        MediaPanel.this, cs.getActionCommand(),
                        cs.getCommandArgument(), me.getWhen(),
                        me.getModifiers());

                } else {
                    ActionSprite as = (ActionSprite) _activeSprite;
                    event = new ActionEvent(
                        MediaPanel.this, ActionEvent.ACTION_PERFORMED,
                        as.getActionCommand(), me.getWhen(), me.getModifiers());
                }
                Controller.postAction(event);
            }
            
            if (!(_activeSprite instanceof HoverSprite)) {
                _activeSprite = null;
            }

            mouseMoved(me);
        }

        // documentation inherited
        public void mouseDragged (MouseEvent me)
        {
            if (_activeSprite instanceof ArmingSprite) {
                ((ArmingSprite) _activeSprite).setArmed(
                    _activeSprite.hitTest(me.getX(), me.getY()));
            }
        }

        // documentation inherited
        public void mouseMoved (MouseEvent me)
        {
            if (_activeSprite instanceof HoverSprite) {
                if (!_activeSprite.hitTest(me.getX(), me.getY())) {
                    ((HoverSprite) _activeSprite).setHovered(false);
                    _activeSprite = null;
                }
            }

            if (_activeSprite == null) {
                Sprite s = getHit(me);
                if (s instanceof HoverSprite) {
                    ((HoverSprite) s).setHovered(true);
                    _activeSprite = s;
                }
            }
        }

        /**
         * Utility method, get the highest sprite, return it
         * if it's enabled.
         */
        protected Sprite getHit (MouseEvent me)
        {
            Sprite s = _spritemgr.getHighestHitSprite(me.getX(), me.getY());
            if (!(s instanceof DisableableSprite) ||
                 ((DisableableSprite) s).isEnabled()) {
                return s;
            } else {
                return null;
            }
        }

        /** The active hover sprite, or action sprite. */
        protected Sprite _activeSprite;
    }
    
    /** The action sprite handler, or null for none. */
    protected ActionSpriteHandler _actionHandler;
    
    /** The number of action/hover sprites being managed. */
    protected int _actionSpriteCount;
    
    // used to render performance metrics
    protected String _perfStatus = "";
    protected Label _perfLabel;
    protected static Rectangle _perfRect;

    /** A debug hook that toggles FPS rendering. */
    protected static RuntimeAdjust.BooleanAdjust _perfDebug =
        new RuntimeAdjust.BooleanAdjust(
            "Toggles frames per second and dirty regions per tick rendering.",
            "narya.media.fps_display", MediaPrefs.config, false) {
            protected void adjusted (boolean newValue) {
                // clear out some things if we're turned off
                if (!newValue) {
                    _perfRect = null;
                }
            }
        };
}
