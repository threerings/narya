//
// $Id: XMLSceneWriter.java,v 1.1 2001/07/24 16:10:19 shaper Exp $

package com.threerings.miso.scene.xml;

import java.io.*;
import java.util.ArrayList;

import com.megginson.sax.DataWriter;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;

import com.samskivert.util.StringUtil;
import com.threerings.miso.Log;
import com.threerings.miso.scene.Scene;

/**
 * The XMLSceneWriter writes a Scene object to an XML file.
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
            dataElement("sid", "" + scene.getId());
            dataElement("version", "" + Scene.getVersion());
            dataElement("hotspots",
                        StringUtil.toString(scene.getHotSpots()));
            dataElement("exits", StringUtil.toString(scene.getExits()));

            startElement("tiles");
            for (int jj = 0; jj < Scene.getNumLayers(); jj++) {
                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute("", "lnum", "", "CDATA", "" + jj);

                startElement("", "layer", "", attrs);
                for (int yy = 0; yy < Scene.getTileHeight(); yy++) {
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
            endDocument();

        } catch (SAXException saxe) {
            Log.warning("Exception writing scene to file " +
                        "[scene=" + scene + ", [fname=" + fname +
                        ", saxe=" + saxe + "].");
        }
    }
}
