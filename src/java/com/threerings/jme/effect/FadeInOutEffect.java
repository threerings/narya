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
import com.jme.scene.shape.Quad;
import com.jme.scene.state.AlphaState;
import com.jme.system.DisplaySystem;

import com.threerings.jme.util.LinearTimeFunction;
import com.threerings.jme.util.TimeFunction;

/**
 * Fades the screen in from a solid color or out to a solid color.
 */
public class FadeInOutEffect extends Quad
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
        super("FadeInOut");

        _color = new ColorRGBA(
            color.r, color.g, color.b, alphaFunc.getValue(0));
        _alphaFunc = alphaFunc;

        // we need to render in the ortho queue
        setRenderQueueMode(Renderer.QUEUE_ORTHO);

        // create a quad the size of the screen
        DisplaySystem ds = DisplaySystem.getDisplaySystem();
        float width = ds.getWidth(), height = ds.getHeight();
        initialize(width, height);
        setLocalTranslation(new Vector3f(width/2, height/2, overUI ? 1f : -1f));
        setDefaultColor(_color);

        AlphaState astate = ds.getRenderer().createAlphaState();
        astate.setBlendEnabled(true);
        astate.setSrcFunction(AlphaState.SB_SRC_ALPHA);
        astate.setDstFunction(AlphaState.DB_ONE_MINUS_SRC_ALPHA);
        astate.setEnabled(true);
        setRenderState(astate);

        updateRenderState();
    }

    /**
     * Allows the fade to be paused.
     */
    public void setPaused (boolean paused)
    {
        _paused = paused;
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
     */
    protected void fadeComplete ()
    {
    }

    protected ColorRGBA _color;
    protected TimeFunction _alphaFunc;
    protected boolean _paused;
}
