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

    public WindowSlider (BWindow window, int mode, float duration)
    {
        super("slider");

        DisplaySystem ds = DisplaySystem.getDisplaySystem();
        int swidth = ds.getWidth(), sheight = ds.getHeight();
        int wwidth = window.getWidth(), wheight = window.getHeight();

        int start = 0, end = 0;
        switch (mode) {
        case FROM_TOP:
            start = sheight+wheight;
            end = (sheight-wheight)/2;
            window.setLocation((swidth-wwidth)/2, start);
            break;

        case FROM_RIGHT:
            start = swidth+wwidth;
            end = (swidth-wwidth)/2;
            window.setLocation(start, (sheight-wheight)/2);
            break;

        case TO_TOP:
            start = (sheight-wheight)/2;
            end = sheight+wheight;
            window.setLocation((swidth-wwidth)/2, start);
            break;

        case TO_RIGHT:
            start = (swidth-wwidth)/2;
            end = swidth+wwidth;
            window.setLocation(start, (sheight-wheight)/2);
            break;
        }

        _mode = mode;
        _window = window;
        _tfunc = new LinearTimeFunction(start, end, duration);
    }

    // documentation inherited
    public void updateGeometricState (float time, boolean initiator)
    {
        super.updateGeometricState(time, initiator);

        if (_mode % 2 == 1) {
            _window.setLocation((int)_tfunc.getValue(time), _window.getY());
        } else {
            _window.setLocation(_window.getX(), (int)_tfunc.getValue(time));
        }

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
}
