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

import java.awt.Color;

/**
 * Utilities to manipulate colors.
 */
public class ColorUtil
{
    /**
     * Blends the two supplied colors.
     *
     * @return a color halfway between the two colors.
     */
    public static final Color blend (Color c1, Color c2)
    {
        return new Color((c1.getRed() + c2.getRed()) >> 1,
                         (c1.getGreen() + c2.getGreen()) >> 1,
                         (c1.getBlue() + c2.getBlue()) >> 1);
    }

    /**
     * Blends the two supplied colors, using the supplied percentage
     * as the amount of the first color to use.
     *
     * @param firstperc The percentage of the first color to use, from 0.0f
     * to 1.0f inclusive.
     */
    public static final Color blend (Color c1, Color c2, float firstperc)
    {
        float p2 = 1.0f - firstperc;
        return new Color((int) (c1.getRed() * firstperc + c2.getRed() * p2),
                 (int) (c1.getGreen() * firstperc + c2.getGreen() * p2),
                 (int) (c1.getBlue() * firstperc + c2.getBlue() * p2));
    }
}
