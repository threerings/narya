//
// $Id: XMLTileSetParser.java,v 1.17 2001/10/12 16:36:58 shaper Exp $

package com.threerings.media.tile;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
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
	    _tset.imgFile = str;

	} else if (qName.equals("rowwidth")) {
	    _tset.rowWidth = StringUtil.parseIntArray(str);

	} else if (qName.equals("rowheight")) {
	    _tset.rowHeight = StringUtil.parseIntArray(str);

	} else if (qName.equals("tilecount")) {
	    _tset.tileCount = StringUtil.parseIntArray(str);

	    // calculate the total number of tiles in the tileset
	    for (int ii = 0; ii < _tset.tileCount.length; ii++) {
		_tset.numTiles += _tset.tileCount[ii];
	    }

	} else if (qName.equals("offsetpos")) {
            getPoint(str, _tset.offsetPos);

        } else if (qName.equals("gapdist")) {
            getPoint(str, _tset.gapDist);

        } else if (qName.equals("tileset")) {
	    // construct the tileset on tag close and add it to the
	    // list of tilesets constructed thus far
	    _tilesets.add(_tset);

	    // prepare to read another tileset object
	    init();
	}
    }

    // documentation inherited
    public void startElement (String uri, String localName,
			      String qName, Attributes attributes)
    {
	_tag = qName;

	if (_tag.equals("tileset")) {
            // construct the new tile set
            _tset = createTileSet();

            // note whether it contains object tiles
            String str = attributes.getValue("layer");
            _tset.isObjectSet = str.toLowerCase().equals(LAYER_OBJECT);

            // get the tile set id
	    _tset.tsid = getInt(attributes.getValue("tsid"));

            // get the tile set name
	    str = attributes.getValue("name");
	    _tset.name = (str == null) ? DEF_NAME : str;

	} else if (_tag.equals("object")) {
	    // TODO: should we bother checking to make sure we only
	    // see <object> tags while within an <objects> tag?
	    int tid = getInt(attributes.getValue("tid"));
	    int wid = getInt(attributes.getValue("width"));
	    int hei = getInt(attributes.getValue("height"));

	    // add the object info to the tileset object hashtable
	    _tset.addObjectInfo(tid, new int[] { wid, hei });
	}
    }

    // documentation inherited
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

    // documentation inherited
    public void characters (char ch[], int start, int length)
    {
	// bail if we're not within a meaningful tag
	if (_tag == null) {
	    return;
	}

  	_chars.append(ch, start, length);
    }

    // documentation inherited
    public List loadTileSets (String fname) throws IOException
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
	_tset = null;
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

    /**
     * Returns the integer represented by the given string or -1 if an
     * error occurred.
     */
    protected int getInt (String str)
    {
	try {
	    return (str == null) ? -1 : Integer.parseInt(str);
	} catch (NumberFormatException nfe) {
	    Log.warning("Malformed integer value [str=" + str + "].");
	    return -1;
	}
    }

    /** Default tileset name. */
    protected static final String DEF_NAME = "Untitled";

    /** String constant denoting an object tile set. */
    protected static final String LAYER_OBJECT = "object";

    /** The XML element tag currently being processed. */
    protected String _tag;

    /** The tilesets constructed thus far. */
    protected ArrayList _tilesets = new ArrayList();

    /** Temporary storage of character data while parsing. */
    protected StringBuffer _chars;

    /** The tile set populated while parsing. */
    protected TileSetImpl _tset;
}
