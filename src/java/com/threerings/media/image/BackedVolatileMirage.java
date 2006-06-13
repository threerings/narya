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

package com.threerings.media.image;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.threerings.media.Log;

/**
 * Provides a volatile mirage that is backed by a buffered image that is
 * not obtained from the image manager but is instead provided at
 * construct time and completely circumvents the image manager's cache. As
 * such, this should not be used unless you know what you're doing.
 */
public class BackedVolatileMirage extends VolatileMirage
{
    /**
     * Creates a mirage with the supplied regeneration informoation and
     * prepared image.
     */
    public BackedVolatileMirage (ImageManager imgr, BufferedImage source)
    {
        super(imgr, new Rectangle(0, 0, source.getWidth(), source.getHeight()));
        _source = source;

        // create our volatile image for the first time
        createVolatileImage();
    }

    // documentation inherited
    protected int getTransparency ()
    {
        return _source.getColorModel().getTransparency();
    }

    // documentation inherited
    protected void refreshVolatileImage ()
    {
        Graphics gfx = null;
        try {
            gfx = _image.getGraphics();
            gfx.drawImage(_source, -_bounds.x, -_bounds.y, null);

        } catch (Exception e) {
            Log.warning("Failure refreshing mirage " + this + ".");
            Log.logStackTrace(e);

        } finally {
            gfx.dispose();
        }
    }

    // documentation inherited
    protected void toString (StringBuilder buf)
    {
        super.toString(buf);
        buf.append(", src=").append(_source);
    }

    protected BufferedImage _source;
}
