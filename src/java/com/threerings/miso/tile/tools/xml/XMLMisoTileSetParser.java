//
// $Id: XMLMisoTileSetParser.java,v 1.5 2001/11/01 01:40:42 shaper Exp $

package com.threerings.miso.tile;

import org.xml.sax.*;

import com.samskivert.util.StringUtil;

import com.threerings.media.ImageManager;
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
    public XMLMisoTileSetParser (ImageManager imgmgr)
    {
        super(imgmgr);
    }

    // documentation inherited
    public void startElement (
        String uri, String localName, String qName, Attributes attributes)
    {
	super.startElement(uri, localName, qName, attributes);

	if (qName.equals("tileset")) {
	    String val = attributes.getValue("layer");
	    _layer = MisoSceneUtil.getLayerIndex(val);
	}
    }

    // documentation inherited
    protected void finishElement (
        String uri, String localName, String qName, String data)
    {
	super.finishElement(uri, localName, qName, data);

	if (qName.equals("passable")) {
	    _passable = StringUtil.parseIntArray(data);
	} else if (qName.equals("tileset")) {
            _passable = null;
            _layer = -1;
        }
    }

    // documentation inherited
    protected TileSet createTileSet ()
    {
        return new MisoTileSet(
            _imgmgr, _info.tsid, _info.name, _info.imgFile, _info.tileCount,
            _info.rowWidth, _info.rowHeight, _info.numTiles, _info.offsetPos,
            _info.gapDist, _info.isObjectSet, _info.objects,
            _layer, _passable);
    }

    protected int _passable[];
    protected int _layer = -1;
}
