//
// $Id: XMLSceneWriter.java,v 1.1 2001/07/23 22:45:04 shaper Exp $

package com.threerings.miso.scene;

import java.io.*;
import java.util.ArrayList;

import com.megginson.sax.DataWriter;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;

import com.samskivert.util.StringUtil;
import com.threerings.miso.Log;

/**
 * The XMLSceneWriter writes a list of Scene objects to an XML file
 * using Megginson Technologies' XMLWriter class functionality.
 */
public class XMLSceneWriter extends DataWriter
{
    /**
     * Construct an XMLSceneWriter object that will be used to write
     * the specified set of scenes to an XML file.
     *
     * @param scenes the scenes to be written.
     */
    public XMLSceneWriter (ArrayList scenes)
    {
        _scenes = scenes;
        setIndentStep(2);
    }

    /**
     * Write the scenes to the specified output file in XML format.
     *
     * @param fname the absolute pathname of the file to write to.
     */
    public void writeToFile (String fname) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(fname);
        setOutput(new OutputStreamWriter(fos));

        try {
            startDocument();
            startElement("scenegroup");

            int size = _scenes.size();
            for (int ii = 0; ii < size; ii++) {
                startElement("scene");

                Scene scene = (Scene)_scenes.get(ii);

                dataElement("name", scene.getName());
                dataElement("sid", "" + scene.getId());
                dataElement("version", "" + scene.VERSION);
                dataElement("hotspots",
                            StringUtil.toString(scene.getHotSpots()));
                dataElement("exits", StringUtil.toString(scene.getExits()));

                startElement("tiles");
                for (int jj = 0; jj < Scene.NUM_LAYERS; jj++) {
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute("", "lnum", "", "CDATA", "" + jj);

                    startElement("", "layer", "", attrs);
                    for (int yy = 0; yy < Scene.TILE_HEIGHT; yy++) {
                        attrs.clear();
                        attrs.addAttribute("", "rownum", "", "CDATA", "" + yy);

                        startElement("", "row", "", attrs);
                        // TODO: write row data.
                        endElement("row");
                    }
                    endElement("layer");
                }
                endElement("tiles");

                endElement("scene");
            }

            endElement("scenegroup");
            endDocument();

        } catch (SAXException saxe) {
            Log.warning("Exception writing scenes to file [fname=" +
                        fname + ", saxe=" + saxe + "].");
        }
    }

    /** The scenes to be written. */
    protected ArrayList _scenes;
}
