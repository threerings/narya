//
// $Id: XMLMisoTileSetParser.java,v 1.1 2001/10/08 21:04:25 shaper Exp $

package com.threerings.miso.tile;

import com.samskivert.util.StringUtil;

import com.threerings.media.tile.*;

/**
 * Extends the base XML tile set parser to construct {@link
 * MisoTileSet} tilesets that provide additional functionality
 * specific to the miso layer.
 */
public class XMLMisoTileSetParser extends XMLTileSetParser
{
    protected void finishElement (String qName, String str)
    {
	super.finishElement(qName, str);

	if (qName.equals("passable")) {
	    ((MisoTileSet.MisoTileSetModel)_model).passable =
		StringUtil.parseIntArray(str);
	}
    }

    protected TileSet createTileSet ()
    {
	return new MisoTileSet();
    }
}
