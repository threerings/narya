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

package com.threerings.puzzle.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.UIManager;

import com.samskivert.swing.Label;
import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.StringUtil;

import com.threerings.media.VirtualMediaPanel;
import com.threerings.media.animation.Animation;
import com.threerings.media.animation.AnimationAdapter;
import com.threerings.media.animation.AnimationArranger;
import com.threerings.media.image.Mirage;
import com.threerings.media.sprite.Sprite;

import com.threerings.parlor.media.ScoreAnimation;

import com.threerings.puzzle.Log;
import com.threerings.puzzle.data.Board;
import com.threerings.puzzle.data.PuzzleCodes;
import com.threerings.puzzle.data.PuzzleConfig;
import com.threerings.puzzle.util.PuzzleContext;

/**
 * The puzzle board view displays a view of a puzzle game.
 */
public abstract class PuzzleBoardView extends VirtualMediaPanel
{
    /**
     * Constructs a puzzle board view.
     */
    public PuzzleBoardView (PuzzleContext ctx)
    {
        super(ctx.getFrameManager());

        // keep this for later
        _ctx = ctx;
    }

    /**
     * Initializes the board with the board dimensions.
     */
    public void init (PuzzleConfig config)
    {
        // save off our bounds
        Dimension bounds = getPreferredSize();
        _bounds = new Rectangle(0, 0, bounds.width, bounds.height);
    }

    /**
     * Sets the board to be displayed.
     */
    public void setBoard (Board board)
    {
        _board = board;
    }

    /**
     * Provides the board view with a reference to its controller so that
     * it may communicate directly rather than by posting actions up the
     * interface hierarchy which sometimes fails if the puzzle board view
     * is hidden before we get a chance to post our actions.
     */
    public void setController (PuzzleController pctrl)
    {
        _pctrl = pctrl;
    }

    /**
     * Sets the background image displayed by the board view.
     */
    public void setBackgroundImage (Mirage image)
    {
        _background = image;
    }

    /**
     * Set whether this puzzle is paused or not.
     * If paused, a label will be displayed with the component's font,
     * which may be set with setFont().
     */
    public void setPaused (boolean paused)
    {
        if (paused) {
            String pmsg = _pctrl.getPauseString();
            pmsg = _ctx.getMessageManager().getBundle(
                PuzzleCodes.PUZZLE_MESSAGE_BUNDLE).xlate(pmsg);
            // create a label using our component's standard font
            _pauseLabel = new Label(pmsg, Label.BOLD | Label.OUTLINE,
                                    Color.WHITE, Color.BLACK,
                                    getFont());
            _pauseLabel.setTargetWidth(_bounds.width);
            _pauseLabel.layout(this);
        } else {
            _pauseLabel = null;
        }
        repaint();
    }

    /**
     * Adds the given animation to the set of animations currently present
     * on the board.  The animation will be added to a list of action
     * animations whose count can be queried with {@link
     * #getActionAnimationCount}. The animation will automatically be
     * removed from the action list when it completes.
     */
    public void addActionAnimation (Animation anim)
    {
        super.addAnimation(anim);

        // remember the animation's existence
        _actionAnims.add(anim);

        // and listen for it to finish so that we can clear it out
        anim.addAnimationObserver(_actionAnimObs);
    }

    // documentation inherited
    public void abortAnimation (Animation anim)
    {
        super.abortAnimation(anim);

        // always check to see if it was action-y
        animationFinished(anim);
    }

    /**
     * Called when a potential action animation is finished.
     */
    protected void animationFinished (Animation anim)
    {
        if (DEBUG_ACTION) {
            Log.info("Animation cleared " + StringUtil.shortClassName(anim) +
                     ":" + _actionAnims.contains(anim));
        }

        // if it WAS an action animation, check for a clear
        if (_actionAnims.remove(anim)) {
            maybeFireCleared();
        }
    }

    /**
     * Adds the given sprite to the set of sprites currently present on
     * the board.  The sprite will be added to a list of action sprites
     * whose count can be queried with {@link #getActionSpriteCount}.  Callers
     * should be sure to remove the sprite when their work with it is done
     * via {@link #removeSprite}.
     */
    public void addActionSprite (Sprite sprite)
    {
        // add the piece to the sprite manager
        addSprite(sprite);

        // note that this piece is interesting
        _actionSprites.add(sprite);
    }

    /**
     * Removes the given sprite from the board.
     */
    public void removeSprite (Sprite sprite)
    {
        super.removeSprite(sprite);

        if (DEBUG_ACTION) {
            Log.info("Sprite cleared " + StringUtil.shortClassName(sprite) +
                     ":" + _actionSprites.contains(sprite));
        }

        // we just always check to see if it was action-y
        if (_actionSprites.remove(sprite)) {
            maybeFireCleared();
        }
    }

    // documentation inherited
    public void clearSprites ()
    {
        super.clearSprites();
        _actionSprites.clear();
    }

    // documentation inherited
    public void clearAnimations ()
    {
        super.clearAnimations();
        _actionAnims.clear();
    }

    /**
     * Returns the number of action animations on the board.
     */
    public int getActionAnimationCount ()
    {
        return _actionAnims.size();
    }

    /**
     * Returns the number of action sprites on the board.
     */
    public int getActionSpriteCount ()
    {
        return _actionSprites.size();
    }

    /**
     * Returns the count of action sprites and animations on the board.
     */
    public int getActionCount ()
    {
        return _actionSprites.size() + _actionAnims.size();
    }

    /**
     * Dumps to the logs, a list of interesting sprites and animations
     * currently active on the puzzle board.
     */
    public void dumpActors ()
    {
        StringUtil.Formatter fmt = new StringUtil.Formatter() {
            public String toString (Object obj) {
                return StringUtil.shortClassName(obj);
            }
        };
        Log.info("Board contents [board=" + StringUtil.shortClassName(this) +
                 ", sprites=" + StringUtil.listToString(_actionSprites, fmt) +
                 ", anims=" + StringUtil.listToString(_actionAnims, fmt) +
                 "].");
    }

    /**
     * Creates and returns an animation displaying the given string with
     * the specified parameters, floating it a short distance up the view.
     *
     * @param score the score text to display.
     * @param color the color of the text.
     * @param font the font whith which to create the score animation.
     * @param x the x-position at which the score is to be placed.
     * @param y the y-position at which the score is to be placed.
     */
    public ScoreAnimation createScoreAnimation (
        String score, Color color, Font font, int x, int y)
    {
        return createScoreAnimation(
            ScoreAnimation.createLabel(score, color, font, this), x, y);
    }

    /**
     * Creates a score animation, allowing derived classes to use custom
     * animations that are customized following a call to
     * {@link #createScoreAnimation}.
     */
    protected ScoreAnimation createScoreAnimation (Label label, int x, int y)
    {
        return new ScoreAnimation(label, x, y);
    }

    /**
     * Positions the supplied animation so as to avoid any active
     * animations previously registered with this method, and adds the
     * animation to the list of animations to be avoided by any future
     * avoid animations.
     */
    public void trackAvoidAnimation (Animation anim)
    {
        // lazy init the arranger
        if (_avoidArranger == null) {
            _avoidArranger = new AnimationArranger();
        }
        _avoidArranger.positionAvoidAnimation(anim, _vbounds);
    }

    // documentation inherited
    public void paintBehind (Graphics2D gfx, Rectangle dirty)
    {
        super.paintBehind(gfx, dirty);

        // render the background
        renderBackground(gfx, dirty);
    }

    /**
     * Fills the background of the board with the background color.
     */
    protected void renderBackground (Graphics2D gfx, Rectangle dirty)
    {
        gfx.setColor(getBackground());
          gfx.fill(dirty);
    }

    // documentation inherited
    public void paintBetween (Graphics2D gfx, Rectangle dirty)
    {
        super.paintBetween(gfx, dirty);
//         PerformanceMonitor.tick(this, "paint");
        renderBoard(gfx, dirty);
    }

    // documentation inherited
    protected void paintInFront (Graphics2D gfx, Rectangle dirty)
    {
        super.paintInFront(gfx, dirty);

        // if the action is paused, indicate as much
        if (_pauseLabel != null) {
            Dimension d = _pauseLabel.getSize();
            _pauseLabel.render(gfx,
                _vbounds.x + (_vbounds.width - d.width) / 2,
                _vbounds.y + (_vbounds.height - d.height) / 2);
        }
    }

    /**
     * Fires a {@link #ACTION_CLEARED} command iff we have no remaining
     * interesting sprites or animations.
     */
    protected void maybeFireCleared ()
    {
        if (DEBUG_ACTION) {
            Log.info("Maybe firing cleared " +
                     getActionCount() + ":" + isShowing());
        }
        if (getActionCount() == 0) {
            // we're probably in the middle of a tick() in an
            // animationDidFinish() call and we want everyone to finish
            // processing their business before we go clearing the action,
            // so we queue this up to be run after the tick is complete
            _ctx.getClient().getRunQueue().postRunnable(new Runnable() {
                public void run () {
                    _pctrl.boardActionCleared();
                }
            });
        }
    }

    /**
     * Renders the board contents to the given graphics context.
     * Sub-classes should implement this method to draw all of their
     * game-specific business.
     */
    protected abstract void renderBoard (Graphics2D gfx, Rectangle dirty);

    /** Our client context. */
    protected PuzzleContext _ctx;

    /** Our puzzle controller. */
    protected PuzzleController _pctrl;

    /** The board data to be displayed. */
    protected Board _board;

    /** The board's bounding rectangle. */
    protected Rectangle _bounds;

    /** The action animations on the board. */
    protected ArrayList _actionAnims = new ArrayList();

    /** The action sprites on the board. */
    protected ArrayList _actionSprites = new ArrayList();

    /** Prevents certain animations from overlapping others. */
    protected AnimationArranger _avoidArranger;

    /** Our background image. */
    protected Mirage _background;

    /** A label to show when the puzzle is paused. */
    protected Label _pauseLabel;

    /** The distance in pixels that score animations float. */
    protected int _scoreDist = DEFAULT_SCORE_DISTANCE;

    /** Listens to our action animations and clears them when they're done. */
    protected AnimationAdapter _actionAnimObs = new AnimationAdapter() {
        public void animationCompleted (Animation anim, long when) {
            animationFinished(anim);
        }
    };

    /** Temporary action debugging. */
    protected static boolean DEBUG_ACTION = false;

    // action state constants
    protected static final int ACTION_GOING = 0;
    protected static final int CLEAR_PENDING = 1;
    protected static final int ACTION_CLEARED = 2;

    /** The default vertical distance to float score animations. */
    protected static final int DEFAULT_SCORE_DISTANCE = 30;
}
