//
// $Id: XMLSceneParser.java,v 1.18 2001/10/11 00:41:27 shaper Exp $

package com.threerings.miso.scene.xml;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.samskivert.util.*;
import com.samskivert.xml.XMLUtil;

import com.threerings.media.tile.*;

import com.threerings.miso.Log;
import com.threerings.miso.scene.*;
import com.threerings.miso.tile.MisoTile;

/**
 * Parse an XML scene description file and construct a scene object.
 * Does not currently perform validation on the input XML stream,
 * though the parsing code assumes the XML document is well-formed.
 *
 * @see XMLSceneWriter
 */
public class XMLSceneParser extends DefaultHandler
{
    public XMLSceneParser (IsoSceneViewModel model, TileManager tilemgr)
    {
	_model = model;
        _tilemgr = tilemgr;
    }

    public void startElement (String uri, String localName,
			      String qName, Attributes attributes)
    {
	_tag = qName;
        if (_tag.equals("layer")) {
            _info.lnum = getInt(attributes.getValue("lnum"));

        } else if (_tag.equals("row")) {
            _info.rownum = getInt(attributes.getValue("rownum"));
	    _info.colstart = getInt(attributes.getValue("colstart"));
        }
    }

    public void endElement (String uri, String localName, String qName)
    {
	// we know we've received the entirety of the character data
        // for the elements we're tracking at this point, so proceed
        // with saving off element values for use when we construct
        // the scene object.
	String str = _chars.toString().trim();

	if (qName.equals("name")) {
	    _info.scene.name = str;

	} else if (qName.equals("version")) {
            int version = getInt(str);
            if (version < 0 || version > XMLSceneVersion.VERSION) {
                Log.warning(
                    "Unrecognized scene file format version, will attempt " + 
                    "to continue parsing file but your mileage may vary " +
                    "[fname=" + _fname + ", version=" + version +
                    ", known_version=" + XMLSceneVersion.VERSION + "].");
            }

	} else if (qName.equals("locations")) {
            int vals[] = StringUtil.parseIntArray(str);
            addLocations(_info.scene.locations, vals);

	} else if (qName.equals("cluster")) {
	    int vals[] = StringUtil.parseIntArray(str);
	    _info.scene.clusters.add(toCluster(_info.scene.locations, vals));

	} else if (qName.equals("portals")) {
            String vals[] = StringUtil.parseStringArray(str);
	    addPortals(_info.scene.portals, _info.scene.locations, vals);

	} else if (qName.equals("row")) {
	    addTileRow(_info, str);

        } else if (qName.equals("scene")) {
	    // nothing for now
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
	if (_tag == null) {
	    return;
	}

  	_chars.append(ch, start, length);
    }

    /**
     * Add the tiles described by the given data to the scene.
     */
    protected void addTileRow (SceneInfo info, String data)
    {
	try {
	    if (info.lnum == MisoScene.LAYER_BASE) {
		readRowData(info, data);
	    } else {
		readSparseRowData(info, data);
	    }

	} catch (TileException te) {
	    Log.warning("Exception reading scene tile data " + 
			"[te=" + te + "].");
	}
    }

    /**
     * Given a string of comma-delimited tuples as (tileset id, tile
     * id), populate the scene info tile array with tiles to suit.
     *
     * @param info the scene info.
     * @param data the tile data.
     */
    protected void readRowData (SceneInfo info, String data)
	throws TileException
    {
        int[] vals = StringUtil.parseIntArray(data);

        // make sure we have a suitable number of tiles
        int validLen = _model.scenewid * 2;
        if (vals.length != validLen) {
            Log.warning(
                "Invalid number of tiles in full row data set, skipping set " +
                "[rownum=" + info.rownum + ", len=" + vals.length +
                ", valid_len=" + validLen + ", data=" + data + "].");
            return;
        }

        // create the tile objects in the tile array
	Tile[][] tiles = info.scene.getTiles(info.lnum);
        for (int xx = 0; xx < vals.length; xx += 2) {
            Tile tile = _tilemgr.getTile(vals[xx], vals[xx + 1]);
            tiles[xx / 2][info.rownum] = tile;
        }
    }

    /**
     * Given a string of comma-delimited tuples as (tileset id, tile
     * id) and having previously set up the scene info object's
     * <code>rownum</code> and <code>colstart</code> member data by
     * retrieving the attribute values when the <code>row</code>
     * element tag was noted, this method then proceeds to populate
     * the info object's tile array with tiles to suit.
     *
     * @param info the scene info.
     * @param data the tile data.
     */
    protected void readSparseRowData (SceneInfo info, String data)
	throws TileException
    {
        int[] vals = StringUtil.parseIntArray(data);

        // make sure we have a suitable number of tiles
        if ((vals.length % 2) == 1) {
            Log.warning(
                "Odd number of tiles in sparse row data set, skipping set " +
                "[rownum=" + info.rownum + ", colstart=" + info.colstart +
                ", len=" + vals.length + "].");
            return;
        }

        // create the tile objects in the tile array
	Tile[][] tiles = info.scene.getTiles(info.lnum);
        for (int xx = 0; xx < vals.length; xx += 2) {
            Tile tile = _tilemgr.getTile(vals[xx], vals[xx + 1]);
	    int xidx = info.colstart + (xx / 2);
            tiles[xidx][info.rownum] = tile;
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
     * Given an array of integer values, add the <code>Location</code>
     * objects represented therein to the given list, constructed from
     * each successive triplet of values as (x, y, orientation) in the
     * integer array.
     *
     * @param vals the integer values.
     */
    protected void addLocations (ArrayList list, int[] vals)
    {
        // make sure we have a seemingly-appropriate number of points
        if ((vals.length % 3) != 0) {
	    return;
	}

	// read in all of the locations and add to the list
        for (int ii = 0; ii < vals.length; ii += 3) {
	    Location loc = new Location(
		vals[ii], vals[ii+1], vals[ii+2]);
	    list.add(loc);
        }
    }

    /**
     * Given an array of string values, add the <code>Portal</code>
     * objects constructed from each successive triplet of values as
     * (locidx, portal name) in the array to the given portal list.
     * The list of <code>Location</code> objects must have already
     * been fully read previously.
     *
     * <p> This is something of a hack since we perhaps ought to parse
     * the original String into its constituent components ourselves,
     * but I'm unable to restrain myself from using the handy
     * <code>StringUtil.toString()</code> method to take care of
     * tokenizing things for us, so, there you have it.
     *
     * @param portals the portal list.
     * @param locs the location list.
     * @param vals the String values.
     */
    protected void addPortals (
	ArrayList portals, ArrayList locs, String[] vals)
    {
        // make sure we have an appropriate number of values
        if ((vals.length % 2) != 0) {
	    return;
	}

	// read in all of the portals
	for (int ii = 0; ii < vals.length; ii += 2) {
	    int locidx = getInt(vals[ii]);

	    // create the portal and add to the list
	    Portal portal = new Portal((Location)locs.get(locidx), vals[ii+1]);
	    portals.add(portal);

	    // upgrade the corresponding location in the location list
	    locs.set(locidx, portal);
	}
    }

    /**
     * Parse the given string as an integer and return the integer
     * value, or -1 if the string is malformed.
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

    protected void init ()
    {
	_chars = new StringBuffer();
	_info = new SceneInfo();
    }	

    /**
     * Parse the specified XML file and return a miso scene object with
     * the data contained therein.
     *
     * @param fname the file name.
     *
     * @return the scene object, or null if an error occurred.
     */
    public EditableMisoScene loadScene (String fname) throws IOException
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
            return _info.scene;

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

    /** The iso scene view data model. */
    protected IsoSceneViewModel _model;

    /** The tile manager object for use in constructing scenes. */
    protected TileManager _tilemgr;

    /** Temporary storage of character data while parsing. */
    protected StringBuffer _chars;

    /** Temporary storage of scene info while parsing. */
    protected SceneInfo _info;

    // TODO: allow specifying the entrance location for a scene in the
    // editor, read/write to XML scene description files.

    /**
     * A class to hold the information gathered while parsing.
     */
    class SceneInfo
    {
	/** The scene populated with data while parsing. */
	public MisoSceneImpl scene;

	/** The current layer number being processed. */
	public int lnum;

	/** The current row number being processed. */
	public int rownum;

	/** The column at which the current row data begins. */
	public int colstart;

	public SceneInfo ()
	{
	    scene = new MisoSceneImpl(_model, null);
	}
    }
}
