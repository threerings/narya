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

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.threerings.media.image.Mirage;

/**
 * Fades an image in or out by varying the alpha level during rendering.
 */
public class FadeImageAnimation extends FadeAnimation
{
    /**
     * Creates an image fading animation.
     */
    public FadeImageAnimation (Mirage image, int x, int y,
                               float alpha, float step, float target)
    {
        super(new Rectangle(x, y, image.getWidth(), image.getHeight()),
              alpha, step, target);
        _image = image;
    }

    // documentation inherited
    protected void paintAnimation (Graphics2D gfx)
    {
        _image.paint(gfx, _bounds.x, _bounds.y);
    }

    protected Mirage _image;
}
