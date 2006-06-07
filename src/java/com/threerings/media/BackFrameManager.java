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

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.image.VolatileImage;

/**
 * A {@link FrameManager} extension that uses a volatile off-screen image
 * to do its rendering.
 */
public class BackFrameManager extends FrameManager
{
    // documentation inherited
    protected void paint (long tickStamp)
    {
        // start out assuming we can do an incremental render
        boolean incremental = true;

        do {
            GraphicsConfiguration gc = _window.getGraphicsConfiguration();

            // create our off-screen buffer if necessary
            if (_backimg == null || _backimg.getWidth() != _window.getWidth() ||
                _backimg.getHeight() != _window.getHeight()) {
                createBackBuffer(gc);
            }

            // make sure our back buffer hasn't disappeared
            int valres = _backimg.validate(gc);

            // if we've changed resolutions, recreate the buffer
            if (valres == VolatileImage.IMAGE_INCOMPATIBLE) {
                Log.info("Back buffer incompatible, recreating.");
                createBackBuffer(gc);
            }

            // if the image wasn't A-OK, we need to rerender the whole
            // business rather than just the dirty parts
            if (valres != VolatileImage.IMAGE_OK) {
                // Log.info("Lost back buffer, redrawing " + valres);
                if (_bgfx != null) {
                    _bgfx.dispose();
                }
                _bgfx = (Graphics2D)_backimg.getGraphics();
                if (_fgfx != null) {
                    _fgfx.dispose();
                    _fgfx = null;
                }
                incremental = false;
            }

            // dirty everything if we're not incrementally rendering
            if (!incremental) {
                _root.getRootPane().revalidate();
                _root.getRootPane().repaint();
            }

            if (!paint(_bgfx)) {
                return;
            }

            // we cache our frame's graphics object so that we can avoid
            // instantiating a new one on every tick
            if (_fgfx == null) {
                _fgfx = (Graphics2D)_window.getGraphics();
            }
            _fgfx.drawImage(_backimg, 0, 0, null);

            // if we loop through a second time, we'll need to rerender
            // everything
            incremental = false;

        } while (_backimg.contentsLost());
    }

    // documentation inherited
    protected void restoreFromBack (Rectangle dirty)
    {
        if (_fgfx == null || _backimg == null) {
            return;
        }
//         Log.info("Restoring from back " + StringUtil.toString(dirty) + ".");
        _fgfx.setClip(dirty);
        _fgfx.drawImage(_backimg, 0, 0, null);
        _fgfx.setClip(null);
    }

    /**
     * Creates the off-screen buffer used to perform double buffered
     * rendering of the animated panel.
     */
    protected void createBackBuffer (GraphicsConfiguration gc)
    {
        // if we have an old image, clear it out
        if (_backimg != null) {
            _backimg.flush();
            _bgfx.dispose();
        }

        // create the offscreen buffer
        int width = _window.getWidth(), height = _window.getHeight();
        _backimg = gc.createCompatibleVolatileImage(width, height);

        // fill the back buffer with white
        _bgfx = (Graphics2D)_backimg.getGraphics();
        _bgfx.fillRect(0, 0, width, height);

        // clear out our frame graphics in case that became invalid for
        // the same reasons our back buffer became invalid
        if (_fgfx != null) {
            _fgfx.dispose();
            _fgfx = null;
        }

//         Log.info("Created back buffer [" + width + "x" + height + "].");
    }

    /** The image used to render off-screen. */
    protected VolatileImage _backimg;

    /** The graphics object from our back buffer. */
    protected Graphics2D _bgfx;

    /** The graphics object from our frame. */
    protected Graphics2D _fgfx;
}
