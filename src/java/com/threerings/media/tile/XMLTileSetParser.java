//
// $Id: XMLTileSetParser.java,v 1.13 2001/08/16 23:14:20 mdb Exp $

package com.threerings.media.tile;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.samskivert.util.*;
import com.samskivert.xml.XMLUtil;
import com.threerings.media.Log;

/**
 * Parse an XML tileset description file and construct tileset objects
 * for each valid description.  Does not currently perform validation
 * on the input XML stream, though the parsing code assumes the XML
 * document is well-formed.
 */
public class XMLTileSetParser extends DefaultHandler
    implements TileSetParser
{
    public void startElement (String uri, String localName,
			      String qName, Attributes attributes)
    {
	_tag = qName;
    }

    public void endElement (String uri, String localName, String qName)
    {
	// we know we've received the entirety of the character data
        // for the elements we're tracking at this point, so proceed
        // with saving off element values for use when we construct
        // the tileset object.
	String str = _chars.toString();

	if (qName.equals("name")) {
	    _info.name = str.trim();

	} else if (qName.equals("tsid")) {
	    try {
		_info.tsid = Integer.parseInt(str);
	    } catch (NumberFormatException nfe) {
		Log.warning("Malformed integer tilesetid [str=" + str + "].");
		_info.tsid = -1;
	    }

	} else if (qName.equals("imagefile")) {
	    _info.imgfile = str;

	} else if (qName.equals("rowwidth")) {
	    _info.rowwidth = StringUtil.parseIntArray(str);

	} else if (qName.equals("rowheight")) {
	    _info.rowheight = StringUtil.parseIntArray(str);

	} else if (qName.equals("tilecount")) {
	    _info.tilecount = StringUtil.parseIntArray(str);

	    // calculate the total number of tiles in the tileset
	    for (int ii = 0; ii < _info.tilecount.length; ii++) {
		_info.numtiles += _info.tilecount[ii];
	    }

	} else if (qName.equals("passable")) {
	    _info.passable = StringUtil.parseIntArray(str);

	} else if (qName.equals("offsetpos")) {
            getPoint(str, _info.offsetpos);

        } else if (qName.equals("gapdist")) {
            getPoint(str, _info.gapdist);

        } else if (qName.equals("tileset")) {
	    // construct the tileset on tag close and add it to the
	    // list of tilesets constructed thus far
	    _tilesets.add(_info.constructTileSet());

	    // prepare to read another tileset object
	    init();
	}

	// note that we're not within a tag to avoid considering any
	// characters during this quiescent time
	_tag = null;

        // and clear out the character data we're gathering
        _chars = new StringBuffer();
    }

    public void characters (char ch[], int start, int length)
    {
	// bail if we're not within a meaningful tag
	if (_tag == null) return;

  	_chars.append(ch, start, length);
    }

    public ArrayList loadTileSets (String fname) throws IOException
    {
	try {
	    InputStream tis = ConfigUtil.getStream(fname);
	    if (tis == null) {
		Log.warning("Couldn't find file [fname=" + fname + "].");
		return null;
	    }

	    _tilesets = new ArrayList();

            // prepare to read a new tileset
            init();

            // read all tileset descriptions from the XML input stream
	    XMLUtil.parse(this, tis);

            return _tilesets;

        } catch (ParserConfigurationException pce) {
  	    throw new IOException(pce.toString());

	} catch (SAXException saxe) {
	    throw new IOException(saxe.toString());
	}
    }

    /**
     * Initialize internal member data used to gather tileset
     * information during parsing.
     */
    protected void init ()
    {
	_chars = new StringBuffer();
	_info = new TileSetInfo();
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

    /** The XML element tag currently being processed. */
    protected String _tag;

    /** The tilesets constructed thus far. */
    protected ArrayList _tilesets = new ArrayList();

    /** Temporary storage of character data while parsing. */
    protected StringBuffer _chars;

    /** Temporary storage of tileset info while parsing. */
    protected TileSetInfo _info;

    /**
     * A class to hold temporary information on a tileset.
     */
    class TileSetInfo
    {
	/** The tileset name. */
	public String name;

	/** The tileset id. */
	public int tsid;

	/** The tileset full image file path. */
	public String imgfile;

	/** The width of the tiles in each row. */
	public int[] rowwidth;

	/** The height of the tiles in each row. */
	public int[] rowheight;

	/** The number of tiles in each row. */
	public int[] tilecount;

	/** The passability of each tile. */
	public int[] passable;

	/** The number of tiles in the tileset. */
	public int numtiles;

	/** The offset position at which tiles begin in the tile image. */
	public Point offsetpos;

	/** The gap distance between each tile in the tile image. */
	public Point gapdist;

	public TileSetInfo ()
	{
	    offsetpos = new Point();
	    gapdist = new Point();
	}	    

	public TileSet constructTileSet ()
	{
	    // if passability is unspecified, default to all passable
	    if (passable == null) {
		passable = new int[numtiles];
		for (int ii = 0; ii < numtiles; ii++) {
		    passable[ii] = 1;
		}
	    }

	    // construct a tileset object using all gathered data
	    return new TileSet(
		name, tsid, imgfile, rowwidth, rowheight, tilecount,
		passable, offsetpos, gapdist);
	}
    }
}
