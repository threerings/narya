//
// $Id: RegionManager.java,v 1.1 2002/04/23 01:16:27 mdb Exp $

package com.threerings.media;

import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages regions (rectangles) that are invalidated in the process of
 * ticking animations and sprites and generally doing other display
 * related business.
 */
public class RegionManager
{
    /**
     * Invalidates the specified region.
     */
    public void invalidateRegion (int x, int y, int width, int height)
    {
        _dirty.add(new Rectangle(x, y, width, height));
    }

    /**
     * Invalidates the specified region (the supplied rectangle will be
     * cloned as the region manager fiddles with the rectangles it uses
     * internally).
     */
    public void invalidateRegion (Rectangle rect)
    {
        _dirty.add((Rectangle)rect.clone());
    }

    /**
     * Adds the supplied rectangle to the dirty regions. Control of the
     * rectangle is given to the region manager as it may choose to bend,
     * fold or mutilate it later. If you don't want the region manager
     * messing with your rectangle, use {@link #invalidateRegion}.
     */
    public void addDirtyRegion (Rectangle rect)
    {
        _dirty.add(rect);
    }

    /**
     * Returns true if dirty regions have been accumulated since the last
     * call to {@link #getDirtyRects}.
     */
    public boolean haveDirtyRegions ()
    {
        return (_dirty.size() > 0);
    }

    /**
     * Merges all outstanding dirty regions into a single list of
     * rectangles and returns that to the caller. Interally, the list of
     * accumulated dirty regions is cleared out and prepared for the next
     * frame.
     */
    public Rectangle[] getDirtyRegions ()
    {
        ArrayList merged = new ArrayList();

        while (_dirty.size() > 0) {
            // pop the next rectangle from the dirty list
            Rectangle mr = (Rectangle)_dirty.remove(0);

            // merge in any overlapping rectangles
            for (int ii = 0; ii < _dirty.size(); ii++) {
                Rectangle r = (Rectangle)_dirty.get(ii);
                if (mr.intersects(r)) {
                    // remove the overlapping rectangle from the list
                    _dirty.remove(ii--);
                    // grow the merged dirty rectangle
                    mr.add(r);
                }
            }

            // add the merged rectangle to the list
            merged.add(mr);
        }

        Rectangle[] rects = new Rectangle[merged.size()];
        merged.toArray(rects);
        return rects;
    }

    /** A list of dirty rectangles. */
    protected ArrayList _dirty = new ArrayList();
}
