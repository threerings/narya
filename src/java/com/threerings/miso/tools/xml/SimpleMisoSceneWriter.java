//
// $Id: SimpleMisoSceneWriter.java,v 1.1 2003/02/12 07:21:50 mdb Exp $

package com.threerings.miso.tools.xml;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.megginson.sax.DataWriter;
import com.samskivert.util.StringUtil;
import com.threerings.tools.xml.NestableWriter;

import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.data.SimpleMisoSceneModel;

/**
 * Generates an XML representation of a {@link SimpleMisoSceneModel}.
 */
public class SimpleMisoSceneWriter implements NestableWriter
{
    /** The element used to enclose scene models written with this
     * writer. */
    public static final String OUTER_ELEMENT = "miso";

    // documentation inherited from interface
    public void write (Object object, DataWriter writer)
        throws SAXException
    {
        SimpleMisoSceneModel model = (SimpleMisoSceneModel)object;
        writer.startElement(OUTER_ELEMENT);
        writeSceneData(model, writer);
        writer.endElement(OUTER_ELEMENT);
    }

    /**
     * Writes just the scene data which is handy for derived classes which
     * may wish to add their own scene data to the scene output.
     */
    protected void writeSceneData (SimpleMisoSceneModel model,
                                   DataWriter writer)
        throws SAXException
    {
        writer.dataElement("width", Integer.toString(model.width));
        writer.dataElement("height", Integer.toString(model.height));
        writer.dataElement("viewwidth", Integer.toString(model.vwidth));
        writer.dataElement("viewheight", Integer.toString(model.vheight));
        writer.dataElement("base",
                           StringUtil.toString(model.baseTileIds, "", ""));

        // write our uninteresting object tile information
        writer.startElement("objects");
        for (int ii = 0; ii < model.objectTileIds.length; ii++) {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("", "tileId", "", "",
                               String.valueOf(model.objectTileIds[ii]));
            attrs.addAttribute("", "x", "", "",
                               String.valueOf(model.objectXs[ii]));
            attrs.addAttribute("", "y", "", "",
                               String.valueOf(model.objectYs[ii]));
            writer.emptyElement("", "object", "", attrs);
        }

        // write our uninteresting object tile information
        for (int ii = 0; ii < model.objectInfo.length; ii++) {
            ObjectInfo info = model.objectInfo[ii];
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("", "tileId", "", "",
                               String.valueOf(info.tileId));
            attrs.addAttribute("", "x", "", "", String.valueOf(info.x));
            attrs.addAttribute("", "y", "", "", String.valueOf(info.y));

            if (!StringUtil.blank(info.action)) {
                attrs.addAttribute("", "action", "", "", info.action);
            }

            if (info.priority != 0) {
                attrs.addAttribute("", "priority", "", "",
                                   String.valueOf(info.priority));
            }

            if (info.sx != 0 || info.sy != 0) {
                attrs.addAttribute("", "sx",  "", "",
                                   String.valueOf(info.sx));
                attrs.addAttribute("", "sy",  "", "",
                                   String.valueOf(info.sy));
                attrs.addAttribute("", "sorient",  "", "",
                                   String.valueOf(info.sorient));
            }

            if (info.zations != 0) {
                attrs.addAttribute("", "zations",  "", "",
                                   String.valueOf(info.zations));
            }
            writer.emptyElement("", "object", "", attrs);
        }
        writer.endElement("objects");
    }
}
