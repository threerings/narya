//
// $Id: MisoSceneWriter.java,v 1.4 2002/01/30 18:28:32 mdb Exp $

package com.threerings.miso.tools.scene.xml;

import org.xml.sax.SAXException;
import com.megginson.sax.DataWriter;
import com.samskivert.util.StringUtil;

import com.threerings.miso.scene.MisoSceneModel;

/**
 * Generates an XML representation of a {@link MisoSceneModel}.
 */
public class MisoSceneWriter
{
    /**
     * Writes the data for the supplied {@link MisoSceneModel} to the XML
     * writer supplied. The writer will already be configured with the
     * appropriate indentation level so that this writer can simply output
     * its elements and allow the calling code to determine where in the
     * greater scene description file the miso data should live.
     */
    public void writeScene (MisoSceneModel model, DataWriter writer)
        throws SAXException
    {
        writer.startElement("miso");
        writeSceneData(model, writer);
        writer.endElement("miso");
    }

    /**
     * Writes just the scene data which is handy for derived classes which
     * may wish to add their own scene data to the scene output.
     */
    protected void writeSceneData (MisoSceneModel model, DataWriter writer)
        throws SAXException
    {
        writer.dataElement("width", Integer.toString(model.width));
        writer.dataElement("height", Integer.toString(model.height));
        writer.dataElement("base",
                           StringUtil.toString(model.baseTileIds, "", ""));
        writer.dataElement("fringe",
                           StringUtil.toString(model.fringeTileIds, "", ""));
        writer.dataElement("object",
                           StringUtil.toString(model.objectTileIds, "", ""));
        writer.dataElement("actions",
                           StringUtil.joinEscaped(model.objectActions));
    }
}
