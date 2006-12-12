//
// $Id$

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
