package com.threerings.media.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class CompositeMirage implements Mirage
{
    public CompositeMirage (Mirage[] mirages)
    {
        _mirages = mirages;
    }

    public CompositeMirage (Mirage mirage1, Mirage mirage2)
    {
        _mirages = new Mirage[]{mirage1, mirage2};
    }

    // documentation inherited from interface Mirage
    public long getEstimatedMemoryUsage ()
    {
        // Return the total memory of our component mirages.
        long mem = 0;
        for (Mirage m : _mirages) {
            mem += m.getEstimatedMemoryUsage();
        }
        
        return mem;
    }

    // documentation inherited from interface Mirage
    public int getHeight ()
    {
        // Return the maximal height of our component mirages.
        int height = 0;
        for (Mirage m : _mirages) {
            height = Math.max(height, m.getHeight());
        }

        return height;
    }

    // documentation inherited from interface Mirage
    public int getWidth ()
    {
        // Return the maximal width of our component mirages.
        int width = 0;
        for (Mirage m : _mirages) {
            width = Math.max(width, m.getWidth());
        }

        return width;
    }

    // documentation inheritd from interface Mirage
    public BufferedImage getSnapshot ()
    {
        BufferedImage img = new BufferedImage(getWidth(), getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = img.createGraphics();

        for (Mirage m : _mirages) {
            BufferedImage snap = m.getSnapshot();
            gfx.drawImage(snap, 0, 0, snap.getWidth(), snap.getHeight(), null);
        }

        return img;
    }

    // documentation inheritd from interface Mirage
    public boolean hitTest (int x, int y)
    {
        // If it hits any of our mirages, it hits us.
        for (Mirage m : _mirages) {
            if (m.hitTest(x, y)) {
                return true;
            }
        }

        return false;
    }

    // documentation inheritd from interface Mirage
    public void paint (Graphics2D gfx, int x, int y)
    {
        // Paint everyone.
        for (Mirage m : _mirages) {
            m.paint(gfx, x, y);
        }
    }

    /** All the component mirages we're made up of. */
    protected Mirage[] _mirages;
}
