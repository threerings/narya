//
// $Id: XMLTileSetParser.java,v 1.11 2001/08/13 19:54:39 shaper Exp $

package com.threerings.miso.tile;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.samskivert.util.*;
import com.samskivert.xml.XMLUtil;
import com.threerings.miso.Log;

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
	    _tsName = str.trim();

	} else if (qName.equals("tsid")) {
	    try {
		_tsTsid = Integer.parseInt(str);
	    } catch (NumberFormatException nfe) {
		Log.warning("Malformed integer tilesetid [str=" + str + "].");
		_tsTsid = -1;
	    }

	} else if (qName.equals("imagefile")) {
	    _tsImgFile = str;

	} else if (qName.equals("rowwidth")) {
	    _tsRowWidth = StringUtil.parseIntArray(str);

	} else if (qName.equals("rowheight")) {
	    _tsRowHeight = StringUtil.parseIntArray(str);

	} else if (qName.equals("tilecount")) {
	    _tsTileCount = StringUtil.parseIntArray(str);

	    // calculate the total number of tiles in the tileset
	    for (int ii = 0; ii < _tsTileCount.length; ii++) {
		_tsNumTiles += _tsTileCount[ii];
	    }

	} else if (qName.equals("passable")) {
	    _tsPassable = StringUtil.parseIntArray(str);

	} else if (qName.equals("offsetpos")) {
            getPoint(str, _tsOffsetPos);

        } else if (qName.equals("gapdist")) {
            getPoint(str, _tsGapDist);

        } else if (qName.equals("tileset")) {
	    constructTileSet();
        }

	// note that we're not within a tag to avoid considering any
	// characters during this quiescent time
	_tag = null;

        // and clear out the character data we're gathering
        _chars = new StringBuffer();
    }

    protected void constructTileSet ()
    {
	// if passability is unspecified, default to all passable
	if (_tsPassable == null) {
	    _tsPassable = new int[_tsNumTiles];
	    for (int ii = 0; ii < _tsNumTiles; ii++) {
		_tsPassable[ii] = 1;
	    }
	}

	// construct the tileset object on tag close
	TileSet tset = new TileSet(
	    _tsName, _tsTsid, _tsImgFile,
	    _tsRowWidth, _tsRowHeight, _tsTileCount, _tsPassable,
	    _tsOffsetPos, _tsGapDist);

	_tilesets.add(tset);

	// prepare to read another tileset object
	init();
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
		return _tilesets;
	    }

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
        _tsOffsetPos = new Point();
        _tsGapDist = new Point();
	_tsPassable = null;
	_tsNumTiles = 0;
	_chars = new StringBuffer();
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

    /** Temporary storage of tileset object values. */
    protected StringBuffer _chars;
    protected String _tsName;
    protected int    _tsTsid;
    protected String _tsImgFile;
    protected int[]  _tsRowWidth, _tsRowHeight, _tsTileCount, _tsPassable;
    protected int    _tsNumTiles;
    protected Point  _tsOffsetPos, _tsGapDist;
}
