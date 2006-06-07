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

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Graphics2D;
import java.awt.ImageCapabilities;
import java.awt.Rectangle;
import java.awt.image.BufferStrategy;

/**
 * A {@link FrameManager} extension that uses a flip-buffer (via {@link
 * BufferStrategy} to do its rendering.
 */
public class FlipFrameManager extends FrameManager
{
    // documentation inherited
    protected void paint (long tickStamp)
    {
        // create our buffer strategy if we don't already have one
        if (_bufstrat == null) {
            BufferCapabilities cap = new BufferCapabilities(
                new ImageCapabilities(true), new ImageCapabilities(true),
                BufferCapabilities.FlipContents.COPIED);
            try {
                _window.createBufferStrategy(2, cap);
            } catch (AWTException ae) {
                Log.warning("Failed creating flip bufstrat: " + ae + ".");
                // fall back to one without custom capabilities
                _window.createBufferStrategy(2);
            }
            _bufstrat = _window.getBufferStrategy();
        }

        // start out assuming we can do an incremental render
        boolean incremental = true;

        do {
            Graphics2D gfx = null;
            try {
                gfx = (Graphics2D)_bufstrat.getDrawGraphics();

                // dirty everything if we're not incrementally rendering
                if (!incremental) {
                    Log.info("Doing non-incremental render; contents lost " +
                             "[lost=" + _bufstrat.contentsLost() +
                             ", rest=" + _bufstrat.contentsRestored() + "].");
                    _root.getRootPane().revalidate();
                    _root.getRootPane().repaint();
                }

                // request to paint our participants and components and bail
                // if they paint nothing
                if (!paint(gfx)) {
                    return;
                }

                // flip our buffer to visible
                _bufstrat.show();

                // if we loop through a second time, we'll need to rerender
                // everything
                incremental = false;

            } finally {
                if (gfx != null) {
                    gfx.dispose();
                }
            }
        } while (_bufstrat.contentsLost());
    }

    // documentation inherited
    protected void restoreFromBack (Rectangle dirty)
    {
        // nothing doing
    }

    /** The buffer strategy used to do our rendering. */
    protected BufferStrategy _bufstrat;
}
