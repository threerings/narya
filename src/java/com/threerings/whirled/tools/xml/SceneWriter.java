//
// $Id: SceneWriter.java,v 1.1 2001/11/29 06:36:29 mdb Exp $

package com.threerings.whirled.tools.xml;

import org.xml.sax.SAXException;
import com.megginson.sax.DataWriter;
import com.samskivert.util.StringUtil;

import com.threerings.whirled.data.SceneModel;

/**
 * Generates an XML representation of a {@link SceneModel}.
 */
public class SceneWriter
{
    /**
     * Writes the data for the supplied {@link SceneModel} to the XML
     * writer supplied. The writer will already be configured with the
     * appropriate indentation level so that this writer can simply output
     * its elements and allow the calling code to determine where in the
     * greater scene description file the scene data should live.
     */
    public void writeScene (SceneModel model, DataWriter writer)
        throws SAXException
    {
        writer.startElement("scene");
        writeSceneData(model, writer);
        writer.endElement("scene");
    }

    /**
     * Writes just the scene data which is handy for derived classes which
     * may wish to add their own scene data to the scene output.
     */
    protected void writeSceneData (SceneModel model, DataWriter writer)
        throws SAXException
    {
        writer.dataElement("sceneId", Integer.toString(model.sceneId));
        writer.dataElement("version", Integer.toString(model.version));
        writer.dataElement("neighborIds",
                           StringUtil.toString(model.neighborIds, "", ""));
    }
}
