//
// $Id: MisoTileSetRepository.java,v 1.1 2002/04/06 01:46:56 mdb Exp $

package com.threerings.miso.tile;

import com.threerings.media.tile.TileSetRepository;

/**
 * Extends the regular tile set repository and provides access to
 * configuration information for the {@link AutoFringer}.
 */
public interface MisoTileSetRepository extends TileSetRepository
{
    /**
     * Returns the configuration information for the {@link AutoFringer}.
     */
    public FringeConfiguration getFringeConfiguration ();
}
