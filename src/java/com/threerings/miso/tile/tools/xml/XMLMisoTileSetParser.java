//
// $Id: XMLMisoTileSetParser.java,v 1.4 2001/10/15 23:53:43 shaper Exp $

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
    public void startElement (
        String uri, String localName, String qName, Attributes attributes)
    {
	super.startElement(uri, localName, qName, attributes);

	if (qName.equals("tileset")) {
	    String val = attributes.getValue("layer");
	    ((MisoTileSetImpl)_tset).layer = MisoSceneUtil.getLayerIndex(val);
	}
    }

    // documentation inherited
    protected void finishElement (
        String uri, String localName, String qName, String data)
    {
	super.finishElement(uri, localName, qName, data);

	if (qName.equals("passable")) {
	    ((MisoTileSetImpl)_tset).passable = StringUtil.parseIntArray(data);
	}
    }

    // documentation inherited
    protected TileSetImpl createTileSet ()
    {
	return new MisoTileSetImpl();
    }
}
