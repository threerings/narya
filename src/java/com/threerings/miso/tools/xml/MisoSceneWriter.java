//
// $Id: MisoSceneWriter.java,v 1.1 2001/11/18 04:09:22 mdb Exp $

package com.threerings.miso.scene.xml;

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
        writer.dataElement("width", Integer.toString(model.width));
        writer.dataElement("height", Integer.toString(model.height));
        writer.dataElement("base", StringUtil.toString(model.baseTileIds));
        writer.dataElement("fringe", StringUtil.toString(model.fringeTileIds));
        writer.dataElement("object", StringUtil.toString(model.objectTileIds));
        writer.endElement("miso");
    }
}
