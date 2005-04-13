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

package com.threerings.media.sprite;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.samskivert.swing.Label;

/**
 * A sprite that uses a label to render itself.  If the label has not been
 * previously laid out (see {@link Label#layout}) it will be done when the
 * sprite is added to a media panel.  If the label is altered after the
 * sprite is created, {@link #updateBounds} should be called.
 */
public class LabelSprite extends Sprite
{
    /**
     * Constructs a label sprite that renders itself with the specified
     * label.
     */
    public LabelSprite (Label label)
    {
        _label = label;
    }

    /**
     * Returns the label displayed by this sprite.
     */
    public Label getLabel ()
    {
        return _label;
    }

    /**
     * Indicates that our label should be rendered with antialiased text.
     */
    public void setAntiAliased (boolean antiAliased)
    {
        _antiAliased = antiAliased;
    }

    /**
     * Updates the bounds of the sprite after a change to the label.
     */
    public void updateBounds ()
    {
        Dimension size = _label.getSize();
        _bounds.width = size.width;
        _bounds.height = size.height;
    }
    
    // documentation inherited
    protected void init ()
    {
        super.init();

        // if our label is not yet laid out, do the deed
        if (!_label.isLaidOut()) {
            Graphics2D gfx = (Graphics2D)_mgr.getMediaPanel().getGraphics();
            if (gfx != null) {
                gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     (_antiAliased) ?
                                     RenderingHints.VALUE_ANTIALIAS_ON :
                                     RenderingHints.VALUE_ANTIALIAS_OFF);
                _label.layout(gfx);
                gfx.dispose();
            }
        }

        // size the bounds to fit our label
        updateBounds();        
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        _label.render(gfx, _bounds.x, _bounds.y);
    }

    /** The label associated with this sprite. */
    protected Label _label;

    /** Whether or not to use anti-aliased rendering. */
    protected boolean _antiAliased;
}
