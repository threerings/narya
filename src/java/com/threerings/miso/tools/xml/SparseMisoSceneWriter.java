//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.miso.tools.xml;

import java.util.Iterator;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.megginson.sax.DataWriter;
import com.samskivert.util.StringUtil;
import com.threerings.tools.xml.NestableWriter;

import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.data.SparseMisoSceneModel.Section;
import com.threerings.miso.data.SparseMisoSceneModel;

/**
 * Generates an XML representation of a {@link SparseMisoSceneModel}.
 */
public class SparseMisoSceneWriter implements NestableWriter
{
    /** The element used to enclose scene models written with this
     * writer. */
    public static final String OUTER_ELEMENT = "miso";

    // documentation inherited from interface
    public void write (Object object, DataWriter writer)
        throws SAXException
    {
        SparseMisoSceneModel model = (SparseMisoSceneModel)object;
        writer.startElement(OUTER_ELEMENT);
        writeSceneData(model, writer);
        writer.endElement(OUTER_ELEMENT);
    }

    /**
     * Writes just the scene data which is handy for derived classes which
     * may wish to add their own scene data to the scene output.
     */
    protected void writeSceneData (SparseMisoSceneModel model,
                                   DataWriter writer)
        throws SAXException
    {
        writer.dataElement("swidth", Integer.toString(model.swidth));
        writer.dataElement("sheight", Integer.toString(model.sheight));
        writer.dataElement("defTileSet", Integer.toString(model.defTileSet));

        writer.startElement("sections");
        for (Iterator iter = model.getSections(); iter.hasNext(); ) {
            Section sect = (Section)iter.next();
            if (sect.isBlank()) {
                continue;
            }
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("", "x", "", "", String.valueOf(sect.x));
            attrs.addAttribute("", "y", "", "", String.valueOf(sect.y));
            attrs.addAttribute("", "width", "", "", String.valueOf(sect.width));
            writer.startElement("", "section", "", attrs);

            writer.dataElement(
                "base", StringUtil.toString(sect.baseTileIds, "", ""));

            // write our uninteresting object tile information
            writer.startElement("objects");
            for (int ii = 0; ii < sect.objectTileIds.length; ii++) {
                attrs = new AttributesImpl();
                attrs.addAttribute("", "tileId", "", "",
                                   String.valueOf(sect.objectTileIds[ii]));
                attrs.addAttribute("", "x", "", "",
                                   String.valueOf(sect.objectXs[ii]));
                attrs.addAttribute("", "y", "", "",
                                   String.valueOf(sect.objectYs[ii]));
                writer.emptyElement("", "object", "", attrs);
            }

            // write our interesting object tile information
            for (int ii = 0; ii < sect.objectInfo.length; ii++) {
                ObjectInfo info = sect.objectInfo[ii];
                attrs = new AttributesImpl();
                attrs.addAttribute("", "tileId", "", "",
                                   String.valueOf(info.tileId));
                attrs.addAttribute("", "x", "", "", String.valueOf(info.x));
                attrs.addAttribute("", "y", "", "", String.valueOf(info.y));

                if (!StringUtil.isBlank(info.action)) {
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
            writer.endElement("section");
        }
        writer.endElement("sections");
    }
}
