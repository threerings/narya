//
// $Id$

package com.threerings.jme.effect;

import com.jme.scene.Node;
import com.jme.system.DisplaySystem;

import com.jmex.bui.BWindow;

import com.threerings.jme.util.LinearTimeFunction;
import com.threerings.jme.util.TimeFunction;

/**
 * Slides a window onto the center of the screen from offscreen or offscreen
 * from the center of the screen.
 */
public class WindowSlider extends Node
{
    public static final int FROM_TOP = 0;
    public static final int FROM_RIGHT = 1;
    public static final int TO_TOP = 2;
    public static final int TO_RIGHT = 3;

    /**
     * Creates a window slider with the specified mode and window that will
     * slide the window either onto or off of the screen in the specified
     * number of seconds.
     *
     * @param dx an offset applied to the starting or destination position
     * along the x axis (starting for sliding off, destination for sliding on).
     * @param dy an offset applied along the y axis.
     */
    public WindowSlider (BWindow window, int mode, float duration,
                         int dx, int dy)
    {
        super("slider");

        DisplaySystem ds = DisplaySystem.getDisplaySystem();
        int swidth = ds.getWidth(), sheight = ds.getHeight();
        int wwidth = window.getWidth(), wheight = window.getHeight();

        int start = 0, end = 0;
        switch (mode) {
        case FROM_TOP:
            start = sheight+wheight;
            end = (sheight-wheight)/2 + dy;
            window.setLocation((swidth-wwidth)/2 + dx, start);
            break;

        case FROM_RIGHT:
            start = swidth+wwidth;
            end = (swidth-wwidth)/2 + dx;
            window.setLocation(start, (sheight-wheight)/2 + dy);
            break;

        case TO_TOP:
            start = (sheight-wheight)/2 + dy;
            end = sheight+wheight;
            window.setLocation((swidth-wwidth)/2 + dx, start);
            break;

        case TO_RIGHT:
            start = (swidth-wwidth)/2 + dx;
            end = swidth+wwidth;
            window.setLocation(start, (sheight-wheight)/2 + dy);
            break;
        }

        _mode = mode;
        _window = window;
        _tfunc = new LinearTimeFunction(start, end, duration);
    }

    /**
     * Allows some number of ticks to be skipped to give the window that is
     * being slid a chance to be layed out before we start keeping track of
     * time. The layout may be expensive and cause the frame rate to drop for a
     * frame or two, thus booching our smooth sliding onto the screen.
     */
    public void setSkipTicks (int skipTicks)
    {
        _skipTicks = skipTicks;
    }

    // documentation inherited
    public void updateGeometricState (float time, boolean initiator)
    {
        super.updateGeometricState(time, initiator);

        // skip ticks as long as we need to
        if (_skipTicks-- > 0) {
            return;
        }

        int winx, winy;
        if (_mode % 2 == 1) {
            winx = (int)_tfunc.getValue(time);
            winy = _window.getY();
        } else {
            winx = _window.getX();
            winy = (int)_tfunc.getValue(time);
        }
        _window.setLocation(winx, winy);

        if (_tfunc.isComplete()) {
            slideComplete();
        }
    }

    /**
     * Called (only once) when we have reached the end of our slide.
     * Automatically detaches this effect from the hierarchy.
     */
    protected void slideComplete ()
    {
        getParent().detachChild(this);
    }

    protected int _mode;
    protected BWindow _window;
    protected TimeFunction _tfunc;

    // skip two frames by default as that generally handles the normal window
    // layout process
    protected int _skipTicks = 2;
}
