//
// $Id: CompiledTileSetManager.java,v 1.5 2001/07/18 22:45:35 shaper Exp $

package com.threerings.miso.tile;

import com.threerings.media.ImageManager;

import java.io.InputStream;
import java.io.IOException;

public class CompiledTileSetManager extends TileSetManagerImpl
{
    public CompiledTileSetManager (ImageManager imgr)
    {
	super(imgr);
    }

    public void loadTileSets (InputStream tis) throws IOException
    {
	// TBD
    }
}
