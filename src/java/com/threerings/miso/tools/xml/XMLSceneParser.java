//
// $Id: XMLSceneParser.java,v 1.13 2001/08/16 20:14:06 shaper Exp $

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
            _info.lnum = getInt(attributes.getValue("lnum"));

        } else if (_tag.equals("row")) {
            _info.rownum = getInt(attributes.getValue("rownum"));

            // get the column start value if present
            String strcs = attributes.getValue("colstart");
            if (strcs != null) _info.colstart = getInt(strcs);
        }
    }

    public void endElement (String uri, String localName, String qName)
    {
	// we know we've received the entirety of the character data
        // for the elements we're tracking at this point, so proceed
        // with saving off element values for use when we construct
        // the scene object.
	if (qName.equals("name")) {
	    _info.name = _chars.toString().trim();

	} else if (qName.equals("version")) {
            int version = getInt(_chars.toString());
            if (version < 0 || version > MisoScene.VERSION) {
                Log.warning(
                    "Unrecognized scene file format version, will attempt " + 
                    "to continue parsing file but your mileage may vary " +
                    "[fname=" + _fname + ", version=" + version +
                    ", known_version=" + MisoScene.VERSION + "].");
            }

	} else if (qName.equals("locations")) {
            int vals[] = StringUtil.parseIntArray(_chars.toString());
            _info.locations = toLocationsList(vals);

	} else if (qName.equals("cluster")) {
	    int vals[] = StringUtil.parseIntArray(_chars.toString());
	    _info.clusters.add(toCluster(_info.locations, vals));

	} else if (qName.equals("portals")) {
            String vals[] = StringUtil.parseStringArray(_chars.toString());
	    _info.portals = toPortalList(_info.locations, vals);

	} else if (qName.equals("row")) {
            if (_info.lnum == MisoScene.LAYER_BASE) {
                readRowData(_info, _chars.toString());
            } else {
                readSparseRowData(_info, _chars.toString());
            }

        } else if (qName.equals("scene")) {
            // construct the scene object on tag close
            _info.constructScene(_tilemgr);
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
     * id), populate the scene info tile array with tiles to suit.
     *
     * @param info the scene info.
     * @param data the tile data.
     */
    protected void readRowData (SceneInfo info, String data)
    {
        int[] vals = StringUtil.parseIntArray(data);

        // make sure we have a suitable number of tiles
        int validLen = MisoScene.TILE_WIDTH * 2;
        if (vals.length != validLen) {
            Log.warning(
                "Invalid number of tiles in full row data set, skipping set " +
                "[rownum=" + info.rownum + ", len=" + vals.length +
                ", valid_len=" + validLen + ", data=" + data + "].");
            return;
        }

        // create the tile objects in the tile array
        for (int xx = 0; xx < vals.length; xx += 2) {
            Tile tile = _tilemgr.getTile(vals[xx], vals[xx + 1]);
            info.tiles[xx / 2][info.rownum][info.lnum] = tile;
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
        for (int xx = 0; xx < vals.length; xx += 2) {
            Tile tile = _tilemgr.getTile(vals[xx], vals[xx + 1]);
	    int xidx = info.colstart + (xx / 2);
            info.tiles[xidx][info.rownum][info.lnum] = tile;
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
     * <code>Portal</code> objects constructed from each successive
     * triplet of values as (locidx, portal name) in the array.  The
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
     * @return the portal list, or null if an error occurred.
     */
    protected ArrayList toPortalList (ArrayList locs, String[] vals)
    {
        // make sure we have an appropriate number of values
        if ((vals.length % 2) != 0) return null;

	// read in all of the portals
	ArrayList portals = new ArrayList();
	for (int ii = 0; ii < vals.length; ii += 2) {
	    int locidx = getInt(vals[ii]);

	    // create the portal and add to the list
	    Portal portal = new Portal((Location)locs.get(locidx), vals[ii+1]);
	    portals.add(portal);

	    // upgrade the corresponding location in the location list
	    locs.set(locidx, portal);
	}

        return portals;
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
    public MisoScene loadScene (String fname) throws IOException
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

    /** The tile manager object for use in constructing scenes. */
    protected TileManager _tilemgr;

    /** Temporary storage of character data while parsing. */
    protected StringBuffer _chars;

    /** Temporary storage of scene info while parsing. */
    protected SceneInfo _info;

    /**
     * A class to hold temporary information on a scene.
     */
    class SceneInfo
    {
	/** The scene name. */
	public String name;

	/** The location list. */
	public ArrayList locations;

	/** The portal list. */
	public ArrayList portals;

	/** The cluster list. */
	public ArrayList clusters;

	/** The tile array. */
	public Tile[][][] tiles;

	/** The current layer number being processed. */
	public int lnum;

	/** The current row number being processed. */
	public int rownum;

	/** The column at which the current row data begins. */
	public int colstart;

	/** The scene object constructed once all scene info is parsed. */
	public MisoScene scene;

	public SceneInfo ()
	{
	    int width = MisoScene.TILE_WIDTH, height = MisoScene.TILE_HEIGHT;
	    tiles = new Tile[width][height][MisoScene.NUM_LAYERS];
	    clusters = new ArrayList();
	}

	public void constructScene (TileManager tilemgr)
	{
	    scene = new MisoScene(
                tilemgr, name, locations, clusters, portals, tiles);
	}
    }
}
