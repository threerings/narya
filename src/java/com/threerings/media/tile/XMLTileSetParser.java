//
// $Id: XMLTileSetParser.java,v 1.18 2001/10/15 23:53:43 shaper Exp $

package com.threerings.media.tile;

import java.awt.Point;
import java.io.*;
import java.util.List;

import org.xml.sax.*;

import com.samskivert.util.*;
import com.samskivert.xml.SimpleParser;

import com.threerings.media.Log;

/**
 * Parse an XML tileset description file and construct tileset objects
 * for each valid description.  Does not currently perform validation
 * on the input XML stream, though the parsing code assumes the XML
 * document is well-formed.
 */
public class XMLTileSetParser extends SimpleParser
    implements TileSetParser
{

    // documentation inherited
    public void startElement (
        String uri, String localName, String qName, Attributes attributes)
    {
	if (qName.equals("tileset")) {
            // construct the new tile set
            _tset = createTileSet();

            // note whether it contains object tiles
            String str = attributes.getValue("layer");
            _tset.isObjectSet =
                (str != null && str.toLowerCase().equals(LAYER_OBJECT));

            // get the tile set id
	    _tset.tsid = parseInt(attributes.getValue("tsid"));

            // get the tile set name
	    str = attributes.getValue("name");
	    _tset.name = (str == null) ? DEF_NAME : str;

	} else if (qName.equals("object")) {
	    // TODO: should we bother checking to make sure we only
	    // see <object> tags while within an <objects> tag?
	    int tid = parseInt(attributes.getValue("tid"));
	    int wid = parseInt(attributes.getValue("width"));
	    int hei = parseInt(attributes.getValue("height"));

	    // add the object info to the tileset object hashtable
	    _tset.addObjectInfo(tid, new int[] { wid, hei });
	}
    }

    // documentation inherited
    protected void finishElement (
        String uri, String localName, String qName, String data)
    {
	if (qName.equals("imagefile")) {
	    _tset.imgFile = data;

	} else if (qName.equals("rowwidth")) {
	    _tset.rowWidth = StringUtil.parseIntArray(data);

	} else if (qName.equals("rowheight")) {
	    _tset.rowHeight = StringUtil.parseIntArray(data);

	} else if (qName.equals("tilecount")) {
	    _tset.tileCount = StringUtil.parseIntArray(data);

	    // calculate the total number of tiles in the tileset
	    for (int ii = 0; ii < _tset.tileCount.length; ii++) {
		_tset.numTiles += _tset.tileCount[ii];
	    }

	} else if (qName.equals("offsetpos")) {
            getPoint(data, _tset.offsetPos);

        } else if (qName.equals("gapdist")) {
            getPoint(data, _tset.gapDist);

        } else if (qName.equals("tileset")) {
	    // add the fully-read tileset to the list of tilesets
	    _tilesets.add(_tset);
	}
    }

    // documentation inherited
    public void loadTileSets (String fname, List tilesets) throws IOException
    {
        // save off tileset list for reference while parsing
        _tilesets = tilesets;

	try {
            parseFile(fname);
        } catch (IOException ioe) {
            Log.warning("Exception parsing tile set descriptions " +
                        "[ioe=" + ioe + "].");
        }
    }

    // documentation inherited
    protected InputStream getInputStream (String fname) throws IOException
    {
        return ConfigUtil.getStream(fname);
    }

    /**
     * Constructs and returns a new tile set object.  Derived classes
     * may override this method to create their own sub-classes of the
     * <code>TileSet</code> object.
     */
    protected TileSetImpl createTileSet ()
    {
        return new TileSetImpl();
    }

    /**
     * Converts a string containing values as (x, y) into the
     * corresponding integer values and populates the given point
     * object.
     *
     * @param str the point values in string format.
     * @param point the point object to populate.
     */
    protected void getPoint (String str, Point point)
    {
        int vals[] = StringUtil.parseIntArray(str);
        point.setLocation(vals[0], vals[1]);
    }

    /** Default tileset name. */
    protected static final String DEF_NAME = "Untitled";

    /** String constant denoting an object tile set. */
    protected static final String LAYER_OBJECT = "object";

    /** The tilesets constructed thus far. */
    protected List _tilesets;

    /** The tile set populated while parsing. */
    protected TileSetImpl _tset;
}
