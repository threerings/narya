//
// $Id: SpotSceneWriter.java,v 1.1 2001/11/29 06:36:28 mdb Exp $

package com.threerings.whirled.tools.spot.xml;

import org.xml.sax.SAXException;
import com.megginson.sax.DataWriter;
import com.samskivert.util.StringUtil;

import com.threerings.whirled.tools.xml.SceneWriter;
import com.threerings.whirled.spot.data.SpotSceneModel;

/**
 * Generates an XML representation of a {@link SpotSceneModel}.
 */
public class SpotSceneWriter extends SceneWriter
{
    /**
     * Writes the data for the supplied {@link SceneModel} to the XML
     * writer supplied. The writer will already be configured with the
     * appropriate indentation level so that this writer can simply output
     * its elements and allow the calling code to determine where in the
     * greater scene description file the scene data should live.
     */
    public void writeScene (SpotSceneModel model, DataWriter writer)
        throws SAXException
    {
        writer.startElement("spot");
        writeSceneData(model, writer);
        writer.endElement("spot");
    }

    /**
     * Writes just the scene data which is handy for derived classes which
     * may wish to add their own scene data to the scene output.
     */
    protected void writeSceneData (SpotSceneModel model, DataWriter writer)
        throws SAXException
    {
        super.writeSceneData(model, writer);

        writer.dataElement("locationIds",
                           StringUtil.toString(model.locationIds, "", ""));
        writer.dataElement("locationX",
                           StringUtil.toString(model.locationX, "", ""));
        writer.dataElement("locationY",
                           StringUtil.toString(model.locationY, "", ""));
        writer.dataElement("locationOrients",
                           StringUtil.toString(model.locationOrients, "", ""));
        writer.dataElement("locationClusters",
                           StringUtil.toString(model.locationClusters, "", ""));
        writer.dataElement("defaultEntranceId",
                           Integer.toString(model.defaultEntranceId));
        writer.dataElement("portalIds",
                           StringUtil.toString(model.portalIds, "", ""));
        writer.dataElement("targetLocIds",
                           StringUtil.toString(model.targetLocIds, "", ""));
    }
}
