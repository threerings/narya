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

package com.threerings.jme;

import com.jme.image.Texture;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Text;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.jme.util.Timer;

/**
 * Tracks and displays render statistics.
 */
public class StatsDisplay extends Node
{
    public StatsDisplay (Renderer renderer)
    {
        super("StatsNode");

        // create an alpha state that will allow us to blend the text on
        // top of whatever else is below
        AlphaState astate = renderer.createAlphaState();
        astate.setBlendEnabled(true);
        astate.setSrcFunction(AlphaState.SB_SRC_ALPHA);
        astate.setDstFunction(AlphaState.DB_ONE);
        astate.setTestEnabled(true);
        astate.setTestFunction(AlphaState.TF_GREATER);
        astate.setEnabled(true);

        // create a font texture
        TextureState font = renderer.createTextureState();
        font.setTexture(
            TextureManager.loadTexture(
                getClass().getClassLoader().getResource(DEFAULT_JME_FONT),
                Texture.MM_LINEAR, Texture.FM_LINEAR));
        font.setEnabled(true);

        _text = new Text("StatsLabel", "");
        _text.setForceView(true);
        _text.setTextureCombineMode(TextureState.REPLACE);

        attachChild(_text);
        setRenderState(font);
        setRenderState(astate);
    }

    public void update (Timer timer, Renderer renderer)
    {
        _stats.setLength(0);
        _stats.append("FPS: ").append((int)timer.getFrameRate());
        _stats.append(" - ").append(renderer.getStatistics(_temp));
        _text.print(_stats);
    }

    protected Text _text;
    protected StringBuffer _stats = new StringBuffer();
    protected StringBuffer _temp = new StringBuffer();

    protected static final String DEFAULT_JME_FONT =
        "rsrc/media/jme/defaultfont.tga";
}
