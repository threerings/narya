//
// $Id: MisoTileManager.java,v 1.1 2002/04/06 01:46:56 mdb Exp $

package com.threerings.miso.tile;

import com.threerings.media.ImageManager;
import com.threerings.media.tile.TileManager;

/**
 * Extends the basic tile manager and provides support for automatically
 * generating fringes in between different types of base tiles in a scene.
 */
public class MisoTileManager extends TileManager
{
    /**
     * Creates a tile manager and provides it with a reference to the
     * image manager from which it will load tileset images.
     *
     * @param imgr the image manager via which the tile manager will
     * decode and cache images.
     */
    public MisoTileManager (ImageManager imgr)
    {
        super(imgr);
    }

    /**
     * Sets the tileset repository that will be used by the tile manager
     * when tiles are requested by tileset id. The miso tile manager
     * requires a miso tileset repository which provides it with
     * information about fringe configuration in addition to the tilesets.
     */
    public void setMisoTileSetRepository (MisoTileSetRepository setrep)
    {
        setTileSetRepository(setrep);

        // now that we have a miso tileset repository, we can create our
        // auto fringer
        _fringer = new AutoFringer(setrep.getFringeConfiguration(), this);
    }

    /**
     * Returns the auto fringer that has been configured for use by this
     * tile manager. This will only be valid if this tile manager has been
     * provided with a miso tileset repository via {@link
     * #setTileSetRepository}.
     */
    public AutoFringer getAutoFringer ()
    {
        return _fringer;
    }

    /** The entity that performs the automatic fringe layer generation. */
    protected AutoFringer _fringer;
}
