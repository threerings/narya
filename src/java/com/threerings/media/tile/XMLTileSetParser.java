//
// $Id: XMLTileSetParser.java,v 1.20 2001/11/01 01:40:42 shaper Exp $

package com.threerings.media.tile;

import java.awt.Point;
import java.io.*;

import org.xml.sax.*;

import com.samskivert.util.*;
import com.samskivert.xml.SimpleParser;

import com.threerings.media.Log;
import com.threerings.media.ImageManager;

/**
 * Parse an XML tileset description file and construct tileset objects
 * for each valid description.  Does not currently perform validation
 * on the input XML stream, though the parsing code assumes the XML
 * document is well-formed.
 */
public class XMLTileSetParser
    extends SimpleParser
    implements TileSetParser
{
    /**
     * Constructs an xml tile set parser.
     */
    public XMLTileSetParser (ImageManager imgmgr)
    {
        _imgmgr = imgmgr;
    }

    // documentation inherited
    public void startElement (
        String uri, String localName, String qName, Attributes attributes)
    {
	if (qName.equals("tileset")) {
            // note whether the tile set contains object tiles
            String str = attributes.getValue("layer");
            _info.isObjectSet =
                (str != null && str.toLowerCase().equals(LAYER_OBJECT));

            // get the tile set id
	    _info.tsid = parseInt(attributes.getValue("tsid"));

            // get the tile set name
	    str = attributes.getValue("name");
	    _info.name = (str == null) ? DEF_NAME : str;

	} else if (qName.equals("object")) {
            // get the object info
	    int tid = parseInt(attributes.getValue("tid"));
	    int wid = parseInt(attributes.getValue("width"));
	    int hei = parseInt(attributes.getValue("height"));

            if (_info.objects == null) {
                _info.objects = new HashIntMap();
            }
            _info.objects.put(tid, new int[] { wid, hei });
	}
    }

    // documentation inherited
    protected void finishElement (
        String uri, String localName, String qName, String data)
    {
	if (qName.equals("imagefile")) {
	    _info.imgFile = data;

	} else if (qName.equals("rowwidth")) {
	    _info.rowWidth = StringUtil.parseIntArray(data);

	} else if (qName.equals("rowheight")) {
	    _info.rowHeight = StringUtil.parseIntArray(data);

	} else if (qName.equals("tilecount")) {
	    _info.tileCount = StringUtil.parseIntArray(data);

	    // calculate the total number of tiles in the tileset
	    for (int ii = 0; ii < _info.tileCount.length; ii++) {
		_info.numTiles += _info.tileCount[ii];
	    }

	} else if (qName.equals("offsetpos")) {
            parsePoint(data, _info.offsetPos);

        } else if (qName.equals("gapdist")) {
            parsePoint(data, _info.gapDist);

        } else if (qName.equals("tileset")) {
            // construct the tile set
            TileSet tset = createTileSet();
            Log.info("Parsed tileset [tset=" + tset + "].");
            // clear the tile set info gathered while parsing
            _info = new TileSetInfo();
	    // add the tileset to the hashtable
	    _tilesets.put(tset.getId(), tset);
	}
    }

    // documentation inherited
    public void loadTileSets (String fname, HashIntMap tilesets)
        throws IOException
    {
        // save off the tileset hashtable
        _tilesets = tilesets;

	try {
            parseFile(fname);
        } catch (IOException ioe) {
            Log.warning("Exception parsing tile set descriptions " +
                        "[ioe=" + ioe + "].");
            Log.logStackTrace(ioe);
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
    protected TileSet createTileSet ()
    {
        return new TileSet(
            _imgmgr, _info.tsid, _info.name, _info.imgFile, _info.tileCount,
            _info.rowWidth, _info.rowHeight, _info.numTiles, _info.offsetPos,
            _info.gapDist, _info.isObjectSet, _info.objects);
    }

    /**
     * Converts a string containing values as (x, y) into the
     * corresponding integer values and populates the given point
     * object.
     *
     * @param str the point values in string format.
     * @param point the point object to populate.
     */
    protected void parsePoint (String str, Point point)
    {
        int vals[] = StringUtil.parseIntArray(str);
        point.setLocation(vals[0], vals[1]);
    }

    /**
     * A class to hold the tile set information gathered while
     * parsing.  See the {@link TileSet} class for documentation on
     * the various parameters.
     */
    protected static class TileSetInfo
    {
        public int tsid;
        public String name;
        public String imgFile;
        public int tileCount[], rowWidth[], rowHeight[];
        public int numTiles;
        public Point offsetPos = new Point();
        public Point gapDist = new Point();
        public boolean isObjectSet;
        public HashIntMap objects;
    }

    /** Default tileset name. */
    protected static final String DEF_NAME = "Untitled";

    /** String constant denoting an object tile set. */
    protected static final String LAYER_OBJECT = "object";

    /** The tilesets constructed thus far. */
    protected HashIntMap _tilesets;

    /** The tile set info populated while parsing. */
    protected TileSetInfo _info = new TileSetInfo();

    /** The image manager. */
    protected ImageManager _imgmgr;
}
