//
// $Id: XMLMisoTileSetParser.java,v 1.3 2001/10/12 00:43:04 shaper Exp $

package com.threerings.miso.tile;

import org.xml.sax.*;

import com.samskivert.util.StringUtil;
import com.threerings.media.tile.*;
import com.threerings.miso.scene.util.MisoSceneUtil;

/**
 * Extends the base XML tile set parser to construct {@link
 * MisoTileSet} tilesets that provide additional functionality
 * specific to the miso layer.
 */
public class XMLMisoTileSetParser extends XMLTileSetParser
{
    // documentation inherited
    public void startElement (String uri, String localName,
			      String qName, Attributes attributes)
    {
	super.startElement(uri, localName, qName, attributes);

	if (qName.equals("tileset")) {
	    String val = attributes.getValue("layer");
	    ((MisoTileSetImpl)_tset).layer = MisoSceneUtil.getLayerIndex(val);
	}
    }

    // documentation inherited
    protected void finishElement (String qName, String str)
    {
	super.finishElement(qName, str);

	if (qName.equals("passable")) {
	    ((MisoTileSetImpl)_tset).passable = StringUtil.parseIntArray(str);
	}
    }

    // documentation inherited
    protected TileSetImpl createTileSet ()
    {
	return new MisoTileSetImpl();
    }
}
