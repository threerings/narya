//
// $Id: ColorUtil.java,v 1.1 2002/03/26 20:09:43 ray Exp $

package com.threerings.media.util;

import java.awt.Color;

/**
 * Utilities to manipulate colors
 */
public class ColorUtil
{
    /**
     * @return a color halfway between the two colors
     */
    public static final Color blend (Color c1, Color c2)
    {
	return new Color((c1.getRed() + c2.getRed()) >> 1,
			 (c1.getGreen() + c2.getGreen()) >> 1,
			 (c1.getBlue() + c2.getBlue()) >> 1);
    }
}
