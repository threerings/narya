//
// $Id: FringeTile.java,v 1.1 2002/04/06 03:41:58 ray Exp $

package com.threerings.miso.tile;

import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Rectangle;
import java.util.ArrayList;

import com.threerings.media.tile.Tile;

/**
 * A fringe tile may be composed of multiple images.
 */
public class FringeTile extends Tile
{
    /**
     * Construct a fringe tile with the specified image.
     */
    public FringeTile (Image img)
    {
        super(img);
    }

    /**
     * Add another image to this FringeTile.
     */
    public void addExtraImage (Image img)
    {
        if (_extras == null) {
            _extras = new ArrayList();
        }
        _extras.add(img);
    }

    // documentation inherited
    public void paint (Graphics2D gfx, Shape dest)
    {
        Rectangle bounds = dest.getBounds();
        int x = bounds.x;
        int y = bounds.y;

        gfx.drawImage(_image, x, y, null);
        if (_extras != null) {
            int size = _extras.size();
            for (int ii=0; ii < size; ii++) {
                gfx.drawImage((Image) _extras.get(ii), x, y, null);
            }
        }
    }

    /** Extra fringe images beyond the one stored in our superclass. */
    protected ArrayList _extras = null;
}
