//
// $Id: XMLTileSetParser.java,v 1.15 2001/10/08 21:04:25 shaper Exp $

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
    /**
     * This method is called when an element is fully parsed.  The
     * complete parsed data for the element is passed in the given
     * string.
     *
     * @param qName the element name.
     * @param str the full parsed data for the element.
     */
    protected void finishElement (String qName, String str)
    {
	if (qName.equals("imagefile")) {
	    _model.imgFile = str;

	} else if (qName.equals("rowwidth")) {
	    _model.rowWidth = StringUtil.parseIntArray(str);

	} else if (qName.equals("rowheight")) {
	    _model.rowHeight = StringUtil.parseIntArray(str);

	} else if (qName.equals("tilecount")) {
	    _model.tileCount = StringUtil.parseIntArray(str);

	    // calculate the total number of tiles in the tileset
	    for (int ii = 0; ii < _model.tileCount.length; ii++) {
		_model.numTiles += _model.tileCount[ii];
	    }

	} else if (qName.equals("offsetpos")) {
            getPoint(str, _model.offsetPos);

        } else if (qName.equals("gapdist")) {
            getPoint(str, _model.gapDist);

        } else if (qName.equals("tileset")) {
	    // construct the tileset on tag close and add it to the
	    // list of tilesets constructed thus far
	    _tilesets.add(_tset);

	    // prepare to read another tileset object
	    init();
	}
    }

    public void startElement (String uri, String localName,
			      String qName, Attributes attributes)
    {
	_tag = qName;

	if (_tag.equals("tileset")) {
	    _model.tsid = getTileSetId(attributes.getValue("tsid"));

	    String str = attributes.getValue("name");
	    _model.name = (str == null) ? DEF_NAME : str;
	}
    }

    public void endElement (String uri, String localName, String qName)
    {
	// we know we've received the entirety of the character data
        // for the elements we're tracking at this point, so proceed
        // with saving off element values for use when we construct
        // the tileset object.
	finishElement(qName, _chars.toString().trim());

	// note that we're not within a tag to avoid considering any
	// characters during this quiescent time
	_tag = null;

        // and clear out the character data we're gathering
        _chars = new StringBuffer();
    }

    public void characters (char ch[], int start, int length)
    {
	// bail if we're not within a meaningful tag
	if (_tag == null) {
	    return;
	}

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
	_tset = createTileSet();
	_model = _tset.getModel();
    }

    /**
     * Constructs and returns a new tile set object.
     */
    protected TileSet createTileSet ()
    {
	return new TileSet();
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

    /**
     * Returns the integer tileset id corresponding to the given
     * string, or <code>DEF_TSID</code> if an error occurred.
     */
    protected int getTileSetId (String str)
    {
	if (str == null) {
	    return DEF_TSID;
	}

	try {
	    return Integer.parseInt(str);
	} catch (NumberFormatException nfe) {
	    Log.warning("Malformed integer tileset id [str=" + str + "].");
	    return DEF_TSID;
	}
    }

    /** Default tileset id. */
    protected static final int DEF_TSID = -1;

    /** Default tileset name. */
    protected static final String DEF_NAME = "Untitled";

    /** The XML element tag currently being processed. */
    protected String _tag;

    /** The tilesets constructed thus far. */
    protected ArrayList _tilesets = new ArrayList();

    /** Temporary storage of character data while parsing. */
    protected StringBuffer _chars;

    /** The tile set whose model is populated while parsing. */
    protected TileSet _tset;

    /** The tile set data model populated while parsing. */
    protected TileSet.TileSetModel _model;
}
