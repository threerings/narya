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

package com.threerings.cast.builder;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import com.threerings.util.DirectionCodes;

import com.threerings.cast.CharacterDescriptor;
import com.threerings.cast.CharacterManager;
import com.threerings.cast.CharacterSprite;
import com.threerings.cast.StandardActions;

/**
 * The sprite panel displays a character sprite centered in the panel
 * suitable for user perusal.
 */
public class SpritePanel extends JPanel
    implements DirectionCodes, BuilderModelListener
{
    /**
     * Constructs the sprite panel.
     */
    public SpritePanel (CharacterManager charmgr, BuilderModel model)
    {
        // save off references
        _charmgr = charmgr;
        _model = model;

        // listen to the builder model so that we can update the
        // sprite when a new component is selected
        _model.addListener(this);
    }

    // documentation inherited
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);
        Graphics2D gfx = (Graphics2D)g;

        if (_sprite != null) {
            // render the sprite
            _sprite.paint(gfx);
        }
    }

    // documentation inherited
    public void doLayout ()
    {
        super.doLayout();
        generateSprite();
        centerSprite();
    }

    // documentation inherited
    public void modelChanged (int event)
    {
        if (event == COMPONENT_CHANGED) {
            generateSprite();
        }
    }

    /**
     * Generates a new character sprite for display to reflect the
     * currently selected character components.
     */
    protected void generateSprite ()
    {
        int components[] = _model.getSelectedComponents();
        CharacterDescriptor desc = new CharacterDescriptor(components, null);
        CharacterSprite sprite = _charmgr.getCharacter(desc);
        setSprite(sprite);
    }

    /**
     * Sets the sprite to be displayed.
     */
    protected void setSprite (CharacterSprite sprite)
    {
        sprite.setActionSequence(StandardActions.STANDING);
        sprite.setOrientation(WEST);
        _sprite = sprite;
        centerSprite();
        repaint();
    }

    /**
     * Sets the sprite's location to render it centered within the panel.
     */
    protected void centerSprite ()
    {
        if (_sprite != null) {
            Dimension d = getSize();
            int swid = _sprite.getWidth(), shei = _sprite.getHeight();
            int x = d.width / 2, y = (d.height + shei) / 2;
            _sprite.setLocation(x, y);
        }
    }

    /** The sprite displayed by the panel. */
    protected CharacterSprite _sprite;

    /** The character manager. */
    protected CharacterManager _charmgr;

    /** The builder model. */
    protected BuilderModel _model;
}
