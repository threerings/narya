//
// $Id: FringeTile.java,v 1.4 2003/01/13 22:55:12 mdb Exp $

package com.threerings.miso.tile;

import java.awt.Graphics2D;
import java.util.ArrayList;

import com.threerings.media.tile.Tile;
import com.threerings.media.image.Mirage;

/**
 * A fringe tile may be composed of multiple images.
 */
public class FringeTile extends Tile
{
    /**
     * Construct a fringe tile with the specified image.
     */
    public FringeTile (Mirage mirage)
    {
        super(mirage);
    }

    /**
     * Add another image to this fringe tile.
     */
    public void addExtraImage (Mirage mirage)
    {
        if (_extras == null) {
            _extras = new ArrayList();
        }
        _extras.add(mirage);
    }

    // documentation inherited
    public void paint (Graphics2D gfx, int x, int y)
    {
        // paint our main image
        super.paint(gfx, x, y);

        if (_extras != null) {
            int size = _extras.size();
            for (int ii = 0; ii < size; ii++) {
                Mirage image = (Mirage)_extras.get(ii);
                image.paint(gfx, x, y);
            }
        }
    }

    /** Extra fringe images beyond the one stored in our superclass. */
    protected ArrayList _extras = null;
}
