//
// $Id: XMLSceneWriter.java,v 1.4 2001/08/09 21:17:06 shaper Exp $

package com.threerings.miso.scene.xml;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;

import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;

import com.megginson.sax.DataWriter;

import com.threerings.miso.Log;
import com.threerings.miso.scene.*;
import com.threerings.miso.tile.Tile;

/**
 * The <code>XMLSceneWriter</code> writes a <code>Scene</code> object
 * to an XML file.
 *
 * <p> The scene id is omitted as the scene id is assigned when the
 * scene template is actually loaded into a server.  Similarly, exit
 * points are specified by scene name rather than scene id.
 */
public class XMLSceneWriter extends DataWriter
{
    /**
     * Construct an XMLSceneWriter object.
     */
    public XMLSceneWriter ()
    {
        setIndentStep(2);
    }

    /**
     * Write the scenes to the specified output file in XML format.
     *
     * @param fname the file to write to.
     */
    public void saveScene (Scene scene, String fname) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(fname);
        setOutput(new OutputStreamWriter(fos));

        try {
            startDocument();

            startElement("scene");

            dataElement("name", scene.getName());
            dataElement("version", "" + Scene.VERSION);
            dataElement("locations", getLocationData(scene));
            dataElement("exits", getExitData(scene));

            startElement("tiles");
            for (int lnum = 0; lnum < Scene.NUM_LAYERS; lnum++) {
                writeLayer(scene, lnum);
            }
            endElement("tiles");

            endElement("scene");
            endDocument();

        } catch (SAXException saxe) {
            Log.warning("Exception writing scene to file " +
                        "[scene=" + scene + ", fname=" + fname +
                        ", saxe=" + saxe + "].");
        }
    }

    /**
     * Output XML detailing the tiles at the specified layer in the
     * given scene.  The first layer is outputted in its entirety,
     * whereas subsequent layers are outputted in a sparse notation
     * that details each contiguous horizontal chunk of tiles in each
     * row separately. 
     *
     * @param scene the scene object.
     * @param lnum the layer number.
     */
    protected void writeLayer (Scene scene, int lnum) throws SAXException
    {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "lnum", "", "CDATA", "" + lnum);

        startElement("", "layer", "", attrs);
        for (int yy = 0; yy < Scene.TILE_HEIGHT; yy++) {
            if (lnum == Scene.LAYER_BASE) {
                writeTileRowData(scene, yy, lnum);
            } else {
                writeSparseTileRowData(scene, yy, lnum);
            }
        }
        endElement("layer");
    }

    /**
     * Return a string representation of the exits in the scene.  Each
     * exit is specified by a comma-delimited tuple of (location
     * index, scene name) values.
     *
     * @param scene the scene object.
     *
     * @return the exits in String format.
     */
    protected String getExitData (Scene scene)
    {
	ArrayList locs = scene.getLocations();
	ArrayList exits = scene.getExits();

        StringBuffer buf = new StringBuffer();
	int size = exits.size();
        for (int ii = 0; ii < size; ii++) {
	    Exit exit = (Exit)exits.get(ii);
	    buf.append(locs.indexOf(exit.loc)).append(",");
	    buf.append(exit.name);
            if (ii < size - 1) buf.append(",");
        }

        return buf.toString();
    }

    /**
     * Return a string representation of the locations in the scene.
     * Each location is specified by a comma-delimited quartet of
     * (spot id, x, y, orientation) values.
     *
     * @return the locations in String format.
     */
    protected String getLocationData (Scene scene)
    {
	ArrayList locs = scene.getLocations();

        StringBuffer buf = new StringBuffer();
	int size = locs.size();
        for (int ii = 0; ii < size; ii++) {
	    Location loc = (Location)locs.get(ii);
	    buf.append(loc.spotid).append(",");
	    buf.append(loc.x).append(",");
	    buf.append(loc.y).append(",");
	    buf.append(loc.orient);
            if (ii < size - 1) buf.append(",");
        }

        return buf.toString();
    }

    /**
     * Return a string representation of the tiles at the specified
     * row and layer in the given scene.  Only <code>len</code> tiles
     * starting at column <code>colstart</code> are included in the
     * string.
     *
     * @param scene the scene object.
     * @param rownum the row number.
     * @param lnum the layer number.
     * @param colstart the first column of data.
     * @param len the number of columns of data.
     *
     * @return the tile data in String format.
     */
    protected String getTileData (Scene scene, int rownum, int lnum,
                                  int colstart, int len)
    {
        StringBuffer buf = new StringBuffer();

        int numtiles = colstart + len;
        for (int ii = colstart; ii < numtiles; ii++) {
            Tile tile = scene.tiles[ii][rownum][lnum];
            if (tile == null) {
                Log.warning("Null tile [x=" + ii + ", rownum=" + rownum +
                            ", lnum=" + lnum + "].");
                continue;
            }

            buf.append(tile.tsid).append(",");
            buf.append(tile.tid);
            if (ii != numtiles - 1) buf.append(",");
        }

        return buf.toString();
    }

    /**
     * Write the row data for a specified row in the scene tile array.
     *
     * <p> The row is written as a <code>row</code> element.  Each
     * tile is specified by a comma-delimited tuple of (tile set id,
     * tile id) numbers, with the associated row number detailed in
     * the <code>rownum</code> attribute of the <code>row</code>
     * element.
     *
     * @param scene the scene object.
     * @param rownum the row in the scene tile array.
     * @param lnum the layer number in the scene tile array.
     */
    protected void writeTileRowData (Scene scene, int rownum, int lnum)
        throws SAXException
    {
        // set up the attributes for this row
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "rownum", "", "CDATA", "" + rownum);

        // output the full row data element
        String data = getTileData(scene, rownum, lnum, 0, Scene.TILE_WIDTH);
        dataElement("", "row", "", attrs, data);
    }

    /**
     * Utility routine used by <code>writeSparseTileRowData</code> to
     * obtain the sets of contiguous tile sets in each row.
     *
     * <p> The search for contiguous tiles starts at the column
     * specified in <code>info[0]</code.
     *
     * <p> Results are returned in the <code>info</code> array as
     * <code>{ colstart, len }</code>.  colstart is -1 if no
     * tiles were found in the rest of the row.
     *
     * @param scene the scene object.
     * @param rownum the row number.
     * @param lnum the layer number.
     * @param info the info array.
     *
     * @return true if any tiles were found, false if not.
     */
    protected boolean
        getSparseColumn (Scene scene, int rownum, int lnum, int info[])
    {
        int start = -1, len = 0;
        for (int xx = info[0]; xx < Scene.TILE_WIDTH; xx++) {
            Tile tile = scene.tiles[xx][rownum][lnum];
            if (tile == null) {
                if (start == -1) continue;
                else break;
            }

            if (start == -1) start = xx;
            len++;
        }

        info[0] = start;
        info[1] = len;

        return (start != -1);
    }

    /**
     * Write the row data for a specified row in the scene tile array
     * in sparse row format.
     *
     * <p> Sparse row format is identical to the format detailed in
     * <code>writeTileRowData()</code> except that a separate
     * <code>row</code> element is outputted for each contiguous set
     * of tiles, with the starting column detailed in the
     * <code>colstart</code> attribute of the <code>row</code>
     * element.
     *
     * @param scene the scene object.
     * @param rownum the row in the scene tile array.
     * @param lnum the layer number in the scene tile array.
     */
    protected void
        writeSparseTileRowData (Scene scene, int rownum, int lnum)
        throws SAXException
    {
        // set up the attributes for this row
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "rownum", "", "CDATA", "" + rownum);
        attrs.addAttribute("", "colstart", "", "CDATA", "0");

        int info[] = new int[] { 0, 0 };
        while (getSparseColumn(scene, rownum, lnum, info)) {
            // update the colstart attribute
            attrs.setAttribute(1, "", "colstart", "", "CDATA", "" + info[0]);

            // output the partial row data element
            String data = getTileData(scene, rownum, lnum, info[0], info[1]);
            dataElement("", "row", "", attrs, data);

            // update the colstart value
            info[0] = info[0] + info[1];
        }
    }
}
