//
// $Id: XMLSceneParser.java,v 1.9 2001/08/13 05:42:36 shaper Exp $

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

	} else if (qName.equals("locations")) {
            int vals[] = StringUtil.parseIntArray(_chars.toString());
            _scLocations = toLocationsList(vals);

	} else if (qName.equals("cluster")) {
	    int vals[] = StringUtil.parseIntArray(_chars.toString());
	    _scClusters.add(toCluster(_scLocations, vals));

	} else if (qName.equals("exits")) {
            String vals[] = StringUtil.parseStringArray(_chars.toString());
	    _scExits = toExitList(_scLocations, vals);

	} else if (qName.equals("row")) {
            if (_scLnum == Scene.LAYER_BASE) {
                readRowData(_chars.toString());
            } else {
                readSparseRowData(_chars.toString());
            }

        } else if (qName.equals("scene")) {
            // construct the scene object on tag close
            _scene = new Scene(
                _tilemgr, _scName, _scLocations, _scClusters,
		_scExits, _scTiles);

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
        for (int xx = 0; xx < vals.length; xx += 2) {
            Tile tile = _tilemgr.getTile(vals[xx], vals[xx + 1]);
            _scTiles[_scColstart + (xx / 2)][_scRownum][_scLnum] = tile;
        }
    }

    /**
     * Given an array of integer values, return a <code>Cluster</code>
     * object containing the location objects identified by location
     * index number in the integer array.
     *
     * @param locs the locations list.
     * @param vals the integer values.
     *
     * @return the cluster object.
     */
    protected Cluster toCluster (ArrayList locs, int[] vals)
    {
	Cluster cluster = new Cluster();
	for (int ii = 0; ii < vals.length; ii++) {
	    cluster.add((Location)locs.get(vals[ii]));
	}
	return cluster;
    }

    /**
     * Given an array of integer values, return a list of the
     * <code>Location</code> objects represented therein, constructed
     * from each successive triplet of values as (x, y, orientation)
     * in the integer array.
     *
     * @param vals the integer values.
     *
     * @return the location list, or null if an error occurred.
     */
    protected ArrayList toLocationsList (int[] vals)
    {
        // make sure we have a seemingly-appropriate number of points
        if ((vals.length % 3) != 0) return null;

	// read in all of the locations and add to the list
	ArrayList list = new ArrayList();
        for (int ii = 0; ii < vals.length; ii += 3) {
	    Location loc = new Location(
		vals[ii], vals[ii+1], vals[ii+2]);
	    list.add(loc);
        }

        return list;
    }

    /**
     * Given an array of string values, return a list of
     * <code>Exit</code> objects constructed from each successive
     * triplet of values as (locidx, scene name) in the array.  The
     * list of <code>Location</code> objects must have already been
     * fully read previously.
     *
     * <p> This is something of a hack since we perhaps ought to parse
     * the original String into its constituent components ourselves,
     * but I'm unable to restrain myself from using the handy
     * <code>StringUtil.toString()</code> method to take care of
     * tokenizing things for us, so, there you have it.
     *
     * @param ArrayList the location list.
     * @param vals the String values.
     *
     * @return the exit list, or null if an error occurred.
     */
    protected ArrayList toExitList (ArrayList locs, String[] vals)
    {
        // make sure we have an appropriate number of values
        if ((vals.length % 2) != 0) return null;

	// read in all of the exits
	ArrayList exits = new ArrayList();
	for (int ii = 0; ii < vals.length; ii += 2) {
	    int locidx = getInt(vals[ii]);

	    // create the exit and add to the list
	    Exit exit = new Exit((Location)locs.get(locidx), vals[ii+1]);
	    exits.add(exit);

	    // upgrade the corresponding location in the location list
	    locs.set(locidx, exit);
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

    protected void init ()
    {
	_chars = new StringBuffer();
	_scene = null;
	int width = Scene.TILE_WIDTH, height = Scene.TILE_HEIGHT;
	_scTiles = new Tile[width][height][Scene.NUM_LAYERS];
	_scLocations = null;
	_scExits = null;
	_scClusters = new ArrayList();
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
	    init();

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
    protected ArrayList _scLocations, _scExits, _scClusters;
    protected Tile[][][] _scTiles;
    protected int _scLnum, _scRownum, _scColstart;
}
