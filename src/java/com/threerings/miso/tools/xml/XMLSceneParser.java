//
// $Id: XMLSceneParser.java,v 1.2 2001/07/24 22:52:02 shaper Exp $

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
    }

    public void endElement (String uri, String localName, String qName)
    {
	// construct the scene object on tag close
	if (qName.equals("scene")) {
            _scene = new Scene(_tilemgr, Scene.SID_INVALID);
            Log.info("Constructed parsed scene [scene=" + _scene + "].");
	}

	// note that we're not within a tag to avoid considering any
	// characters during this quiescent time
	_tag = null;
    }

    public void characters (char ch[], int start, int length)
    {
	// bail if we're not within a meaningful tag
	if (_tag == null) return;

  	String str = String.copyValueOf(ch, start, length);

	// store the value associated with the current tag for use
	// when we construct the tileset object.
	if (_tag.equals("name")) {
	    _scName = str;

	} else if (_tag.equals("version")) {
            int version = getInt(str);
            if (version < 0 || version > Scene.VERSION) {
                Log.warning(
                    "Unrecognized scene file format version, will attempt " + 
                    "to continue parsing file but your mileage may vary " +
                    "[fname=" + _fname + ", version=" + version +
                    ", known_version=" + Scene.VERSION + "].");
            }

	} else if (_tag.equals("hotspots")) {
            _scHotspots = toPointArray(StringUtil.parseIntArray(str));

	} else if (_tag.equals("exits")) {
	    _scExits = toExitPointArray(StringUtil.parseStringArray(str));
	}

        // TODO:
        // tiles
        //   layer lnum="0"
        //     row <rownum="X">
        //   layer lnum="1"
        //     row <rownum="X" colstart="Y">
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

            // read the XML input stream and construct the scene object
            _scene = null;
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

    // temporary storage of scene object values
    protected String _scName;
    protected Point[] _scHotspots;
    protected ExitPoint[] _scExits;
    protected Tile[][][] _scTiles;
}
