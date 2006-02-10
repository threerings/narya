//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.jme.effect;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.AlphaState;
import com.jme.system.DisplaySystem;

import com.threerings.jme.util.LinearTimeFunction;
import com.threerings.jme.util.TimeFunction;

/**
 * Fades a supplied quad (or one that covers the screen) in from a solid color
 * or out to a solid color.
 */
public class FadeInOutEffect extends Node
{
    public FadeInOutEffect (ColorRGBA color, float startAlpha, float endAlpha,
                            float duration, boolean overUI)
    {
        this(color, new LinearTimeFunction(startAlpha, endAlpha, duration),
             overUI);
    }

    public FadeInOutEffect (ColorRGBA color, TimeFunction alphaFunc,
                            boolean overUI)
    {
        this(null, color, alphaFunc, overUI);
        setQuad(createCurtain());
    }

    public FadeInOutEffect (Quad quad, ColorRGBA color, TimeFunction alphaFunc,
                            boolean overUI)
    {
        super("FadeInOut");

        _color = new ColorRGBA(
            color.r, color.g, color.b, alphaFunc.getValue(0));
        _alphaFunc = alphaFunc;

        if (quad != null) {
            setQuad(quad);
        }

        setRenderQueueMode(Renderer.QUEUE_ORTHO);
        setZOrder(overUI ? -1 : 1);

        DisplaySystem ds = DisplaySystem.getDisplaySystem();
        AlphaState astate = ds.getRenderer().createAlphaState();
        astate.setBlendEnabled(true);
        astate.setSrcFunction(AlphaState.SB_SRC_ALPHA);
        astate.setDstFunction(AlphaState.DB_ONE_MINUS_SRC_ALPHA);
        astate.setEnabled(true);
        setRenderState(astate);

        updateRenderState();
    }

    /**
     * Configures the quad that will be faded in or out.
     */
    public void setQuad (Quad quad)
    {
        attachChild(quad);
        quad.setDefaultColor(_color);
    }

    /**
     * Allows the fade to be paused.
     */
    public void setPaused (boolean paused)
    {
        _paused = paused;
    }

    /**
     * Indicates whether or not the fade is paused.
     */
    public boolean isPaused ()
    {
        return _paused;
    }

    // documentation inherited
    public void updateGeometricState (float time, boolean initiator)
    {
        super.updateGeometricState(time, initiator);
        if (_paused) {
            return;
        }

        float alpha = _alphaFunc.getValue(time);
        _color.a = Math.min(1f, Math.max(0f, alpha));
        if (_alphaFunc.isComplete()) {
            fadeComplete();
        }
    }

    /**
     * Called (only once) when we have reached the end of our fade.
     * Automatically detaches this effect from the hierarchy.
     */
    protected void fadeComplete ()
    {
        getParent().detachChild(this);
    }

    /**
     * Creates a quad that covers the entire screen for full-screen fades.
     */
    protected Quad createCurtain ()
    {
        // create a quad the size of the screen
        DisplaySystem ds = DisplaySystem.getDisplaySystem();
        float width = ds.getWidth(), height = ds.getHeight();
        Quad curtain = new Quad("curtain", width, height);
        curtain.setLocalTranslation(new Vector3f(width/2, height/2, 0f));
        return curtain;
    }

    protected ColorRGBA _color;
    protected TimeFunction _alphaFunc;
    protected boolean _paused;
}
