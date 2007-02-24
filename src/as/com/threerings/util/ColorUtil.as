//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.util {

/**
 * Color utility methods.
 *
 * See also mx.utils.ColorUtil.
 */
public class ColorUtil
{
    /**
     * Blend the two colors, either 50-50 or according to the ratio specified.
     */
    public static function blend (
        first :uint, second :uint, firstPerc :Number = 0.5) :uint
    {
        var secondPerc :Number = 1 - firstPerc;

        var result :uint = 0;
        for (var shift :int = 0; shift <= 16; shift += 8) {
            var c1 :uint = (first >> shift) & 0xFF;
            var c2 :uint = (second >> shift) & 0xFF;
            result |= uint(Math.max(0, Math.min(255,
                (c1 * firstPerc) + (c2 * secondPerc)))) << shift;
        }
        return result;
    }
}
}
