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

import java.awt.Rectangle;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.threerings.media.util.MathUtil;

/**
 * Provides a {@link BoundedRangeModel} interface to a virtual media panel
 * so that it can easily be wired up to scroll bars or other scrolling
 * controls.
 */
public class VirtualRangeModel
    implements ChangeListener
{
    /**
     * Creates a virtual media panel range model that will interact with
     * the supplied virtual media panel.
     */
    public VirtualRangeModel (VirtualMediaPanel panel)
    {
        _panel = panel;

        // listen to our range models and scroll our badself
        _horizRange.addChangeListener(this);
        _vertRange.addChangeListener(this);
    }

    /**
     * Informs the virtual range model the extent of the area over which
     * we can scroll.
     */
    public void setScrollableArea (int x, int y, int width, int height)
    {
        Rectangle vbounds = _panel.getViewBounds();
        int hmax = x + width, vmax = y + height, value =
            MathUtil.bound(x, _horizRange.getValue(), hmax - vbounds.width);
        _horizRange.setRangeProperties(value, vbounds.width, x, hmax, false);
        value = MathUtil.bound(y, _vertRange.getValue(), vmax - vbounds.height);
        _vertRange.setRangeProperties(value, vbounds.height, y, vmax, false);
    }

    /**
     * Returns a range model that controls the scrollability of the scene
     * in the horizontal direction.
     */
    public BoundedRangeModel getHorizModel ()
    {
        return _horizRange;
    }

    /**
     * Returns a range model that controls the scrollability of the scene
     * in the vertical direction.
     */
    public BoundedRangeModel getVertModel ()
    {
        return _vertRange;
    }

    // documentation inherited from interface ChangeListener
    public void stateChanged (ChangeEvent e)
    {
        _panel.setViewLocation(_horizRange.getValue(), _vertRange.getValue());
    }

    protected VirtualMediaPanel _panel;
    protected BoundedRangeModel _horizRange = new DefaultBoundedRangeModel();
    protected BoundedRangeModel _vertRange = new DefaultBoundedRangeModel();
}
