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

package com.threerings.media.animation;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import com.samskivert.swing.Label;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.Log;

/**
 * Does something extraordinary.
 */
public class FadeLabelAnimation extends FadeAnimation
{
    /**
     * Creates a label fading animation.
     */
    public FadeLabelAnimation (Label label, int x, int y,
                               float alpha, float step, float target)
    {
        super(new Rectangle(x, y, 0, 0), alpha, step, target);
        _label = label;
    }

    /**
     * Indicates that our label should be rendered with antialiased text.
     */
    public void setAntiAliased (boolean antiAliased)
    {
        _antiAliased = antiAliased;
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
                                     _antiAliased ?
                                     RenderingHints.VALUE_ANTIALIAS_ON :
                                     RenderingHints.VALUE_ANTIALIAS_OFF);
                _label.layout(gfx);
                gfx.dispose();
            }
        }

        // size the bounds to fit our label
        Dimension size = _label.getSize();
        _bounds.width = size.width;
        _bounds.height = size.height;
    }

    // documentation inherited
    protected void paintAnimation (Graphics2D gfx)
    {
        Object ohints = null;
        if (_antiAliased) {
            ohints = SwingUtil.activateAntiAliasing(gfx);
        }
        _label.render(gfx, _bounds.x, _bounds.y);
        if (_antiAliased) {
            SwingUtil.restoreAntiAliasing(gfx, ohints);
        }
    }

    /** The label we are rendering. */
    protected Label _label;

    /** Whether or not to use anti-aliased rendering. */
    protected boolean _antiAliased;
}
