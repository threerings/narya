//
// $Id: XMLSceneParser.java,v 1.3 2001/07/25 01:38:08 shaper Exp $

package com.threerings.miso.scene.xml;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.samskivert.util.*;
import com.samskivert.xml.XMLUtil;
import com.threerings.miso.Log;
import com.threerings.miso.scene.*;
import com.threerings.miso.tile.*;

/**
 * Parse an XML scene description file and construct a scene object.
 * Does not currently perform validation on the input XML stream,
 * though the parsing code assumes the XML document is well-formed.
 *
 * @see XMLSceneWriter
 */
public class XMLSceneParser extends DefaultHandler
{
    public XMLSceneParser (TileManager tilemgr)
    {
        _tilemgr = tilemgr;
    }

    public void startElement (String uri, String localName,
			      String qName, Attributes attributes)
    {
	_tag = qName;
        if (_tag.equals("layer")) {
            _scLnum = getInt(attributes.getValue("lnum"));

        } else if (_tag.equals("row")) {
            _scRownum = getInt(attributes.getValue("rownum"));

            // get the column start value if present
            String strcs = attributes.getValue("colstart");
            if (strcs != null) _scColstart = getInt(strcs);
        }
    }

    public void endElement (String uri, String localName, String qName)
    {
	// we know we've received the entirety of the character data
        // for the elements we're tracking at this point, so proceed
        // with saving off element values for use when we construct
        // the scene object.
	if (qName.equals("name")) {
	    _scName = _chars.toString().trim();

	} else if (qName.equals("version")) {
            int version = getInt(_chars.toString());
            if (version < 0 || version > Scene.VERSION) {
                Log.warning(
                    "Unrecognized scene file format version, will attempt " + 
                    "to continue parsing file but your mileage may vary " +
                    "[fname=" + _fname + ", version=" + version +
                    ", known_version=" + Scene.VERSION + "].");
            }

	} else if (qName.equals("hotspots")) {
            int vals[] = StringUtil.parseIntArray(_chars.toString());
            _scHotspots = toPointArray(vals);

	} else if (qName.equals("exits")) {
            String vals[] = StringUtil.parseStringArray(_chars.toString());
	    _scExits = toExitPointArray(vals);

	} else if (qName.equals("row")) {
            if (_scLnum == Scene.LAYER_BASE) {
                readRowData(_chars.toString());
            } else {
                readSparseRowData(_chars.toString());
            }

        } else if (qName.equals("scene")) {
            // construct the scene object on tag close
            _scene = new Scene(_tilemgr, Scene.SID_INVALID, _scName,
                               _scHotspots, _scExits, _scTiles);
            Log.info("Constructed parsed scene [scene=" + _scene + "].");
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

    /**
     * Given a string of comma-delimited tuples as (tileset id, tile
     * id), populate the <code>_scTiles</code> tile array with tiles
     * to suit.
     *
     * @param data the tile data.
     */
    protected void readRowData (String data)
    {
        int[] vals = StringUtil.parseIntArray(data);

        // make sure we have a suitable number of tiles
        int validLen = Scene.TILE_WIDTH * 2;
        if (vals.length != validLen) {
            Log.warning(
                "Invalid number of tiles in full row data set, skipping set " +
                "[rownum=" + _scRownum + ", len=" + vals.length +
                ", valid_len=" + validLen + ", data=" + data + "].");
            return;
        }

        // create the tile objects in the tile array
        for (int xx = 0; xx < vals.length; xx += 2) {
            Tile tile = _tilemgr.getTile(vals[xx], vals[xx + 1]);
            _scTiles[xx / 2][_scRownum][_scLnum] = tile;
        }
    }

    /**
     * Given a string of comma-delimited tuples as (tileset id, tile
     * id) and having previously set up the <code>_scRownum</code> and
     * <code>_scColstart</code> member data by retrieving the
     * attribute values when the <code>row</code> element tag was
     * noted, this method then proceeds to populate the
     * <code>_scTiles</code> tile array with tiles to suit.
     *
     * @param data the tile data.
     */
    protected void readSparseRowData (String data)
    {
        int[] vals = StringUtil.parseIntArray(data);

        // make sure we have a suitable number of tiles
        if ((vals.length % 2) == 1) {
            Log.warning(
                "Odd number of tiles in sparse row data set, skipping set " +
                "[rownum=" + _scRownum + ", colstart=" + _scColstart +
                ", len=" + vals.length + "].");
            return;
        }

        // create the tile objects in the tile array
        Log.info("Sparse row data [colstart=" + _scColstart +
                 ", rownum=" + _scRownum + ", lnum=" + _scLnum + "].");
        for (int xx = 0; xx < vals.length; xx += 2) {
            Tile tile = _tilemgr.getTile(vals[xx], vals[xx + 1]);
            _scTiles[_scColstart + (xx / 2)][_scRownum][_scLnum] = tile;
        }
    }

    /**
     * Given an array of integer values, return a Point array
     * constructed from each successive tuple of values as (x, y) in
     * the array.
     *
     * @param vals the integer values.
     *
     * @return the point array, or null if an error occurred.
     */
    protected Point[] toPointArray (int[] vals)
    {
        // make sure we have an even number of points
        if ((vals.length % 2) == 1) return null;

        // pull the point coordinates out of the int array
        Point[] points = new Point[vals.length / 2];
        for (int ii = 0; ii < vals.length; ii += 2) {
            int idx = ii / 2;
            points[idx] = new Point(vals[ii], vals[ii + 1]);
        }

        return points;
    }

    /**
     * Given an array of String values, return an ExitPoint array
     * constructed from each successive triplet of values as (x, y,
     * scene name) in the array.
     *
     * <p> This is something of a hack since we perhaps ought to parse
     * the original String into its constituent components ourselves,
     * but I'm unable to restrain myself from using the handy
     * <code>StringUtil.toString()</code> method to take care of
     * tokenizing things for us, so, there you have it.
     *
     * @param vals the String values.
     *
     * @return the ExitPoint array, or null if an error occurred.
     */
    protected ExitPoint[] toExitPointArray (String[] vals)
    {
        // make sure we have an appropriate number of values
        if ((vals.length % 3) != 0) return null;

        // pull the point values out of the string array
        ExitPoint[] exits = new ExitPoint[vals.length / 3];
        for (int ii = 0; ii < vals.length; ii += 3) {
            int idx = ii / 3;
            exits[idx] = new ExitPoint();
            exits[idx].x = (byte)getInt(vals[ii]);
            exits[idx].y = (byte)getInt(vals[ii + 1]);
            exits[idx].name = vals[ii + 2];

            // scene id is only valid at runtime
            exits[idx].sid = Scene.SID_INVALID;
        }

        return exits;
    }

    /**
     * Parse the given string as an integer and return the integer
     * value, or -1 if the string is malformed.
     */
    protected int getInt (String str)
    {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            Log.warning("Malformed integer value [str=" + str + "].");
            return -1;
        }
    }

    /**
     * Parse the specified XML file and return a Scene object with the
     * data contained therein.
     *
     * @param fname the file name.
     *
     * @return the scene object, or null if an error occurred.
     */
    public Scene loadScene (String fname) throws IOException
    {
        _fname = fname;

	try {
            // get the file input stream
	    FileInputStream fis = new FileInputStream(fname);
            BufferedInputStream bis = new BufferedInputStream(fis);

            // prepare temporary data storage for parsing
            _chars = new StringBuffer();
            _scene = null;
            int width = Scene.TILE_WIDTH, height = Scene.TILE_HEIGHT;
            _scTiles = new Tile[width][height][Scene.NUM_LAYERS];

            // read the XML input stream and construct the scene object
	    XMLUtil.parse(this, bis);

            // return the final scene object
            return _scene;

        } catch (ParserConfigurationException pce) {
  	    throw new IOException(pce.toString());

	} catch (SAXException saxe) {
	    throw new IOException(saxe.toString());
	}
    }

    /** The file currently being processed. */
    protected String _fname;

    /** The XML element tag currently being processed. */
    protected String _tag;

    /** The scene object constructed as each scene file is read. */
    protected Scene _scene;

    /** The tile manager object for use in constructing scenes. */
    protected TileManager _tilemgr;

    // temporary storage of scene object values and data
    protected StringBuffer _chars;
    protected String _scName;
    protected Point[] _scHotspots;
    protected ExitPoint[] _scExits;
    protected Tile[][][] _scTiles;
    protected int _scLnum, _scRownum, _scColstart;
}
