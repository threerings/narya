//
// $Id: Mirage.java,v 1.2 2003/01/17 02:30:21 mdb Exp $

package com.threerings.media.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Provides an interface via which images can be accessed in a way that
 * allows them to optionally be located in video memory where that affords
 * performance improvements.
 */
public interface Mirage
{
    /**
     * Renders this mirage at the specified position in the supplied
     * graphics context.
     */
    public void paint (Graphics2D gfx, int x, int y);

    /**
     * Returns the width of this mirage.
     */
    public int getWidth ();

    /**
     * Returns the height of this mirage.
     */
    public int getHeight ();

    /**
     * Returns true if this mirage contains a non-transparent pixel at the
     * specified coordinate.
     */
    public boolean hitTest (int x, int y);

    /**
     * Returns a snapshot of this mirage as a buffered image. The snapshot
     * should <em>not</em> be modified by the caller.
     */
    public BufferedImage getSnapshot ();

    /**
     * Returns an estimate of the memory consumed by this mirage's image
     * raster data.
     */
    public long getEstimatedMemoryUsage ();
}
