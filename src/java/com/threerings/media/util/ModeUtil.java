//
// $Id: ModeUtil.java,v 1.3 2002/05/09 18:43:56 mdb Exp $

package com.threerings.media.util;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * Display mode related utilities.
 */
public class ModeUtil
{
    /**
     * Gets a display mode that matches the specified parameters. The
     * screen resolution must match the specified resolution exactly, the
     * specified desired depth will be used if it is available, and if
     * not, the highest depth greater than or equal to the specified
     * minimum depth is used. The highest refresh rate available for the
     * desired mode is also used.
     */
    public static DisplayMode getDisplayMode (
        GraphicsDevice gd, int width, int height,
        int desiredDepth, int minimumDepth)
    {
        DisplayMode[] modes = gd.getDisplayModes();
        final int ddepth = desiredDepth;

        // we sort modes in order of desirability
        Comparator mcomp = new Comparator () {
            public int compare (Object o1, Object o2) {
                DisplayMode m1 = (DisplayMode)o1;
                DisplayMode m2 = (DisplayMode)o2;
                int bd1 = m1.getBitDepth(), bd2 = m2.getBitDepth();
                int rr1 = m1.getRefreshRate(), rr2 = m2.getRefreshRate();

                // prefer the desired depth
                if (bd1 == ddepth && bd2 != ddepth) {
                    return -1;
                } else if (bd2 == ddepth && bd1 != ddepth) {
                    return 1;
                }

                // otherwise prefer higher depths
                if (bd1 != bd2) {
                    return bd2 - bd1;
                }

                // for same bitrates, prefer higher refresh rates
                return rr2 - rr1;
            }

            public boolean equals (Object obj) {
                return this == obj;
            }
        };

        // but we only add modes that meet our minimum requirements
        TreeSet mset = new TreeSet(mcomp);
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].getWidth() == 800 &&
                modes[i].getHeight() == 600 &&
                modes[i].getBitDepth() >= minimumDepth &&
                modes[i].getRefreshRate() <= 75) {
                mset.add(modes[i]);
            }
        }

        return (mset.size() > 0) ? (DisplayMode)mset.first() : null;
    }
}
