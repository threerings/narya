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

package com.threerings.whirled.spot.tools.xml;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.megginson.sax.DataWriter;
import com.threerings.tools.xml.NestableWriter;

import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.SpotSceneModel;
import com.threerings.whirled.spot.tools.EditablePortal;

/**
 * Generates an XML representation of a {@link SpotSceneModel}.
 */
public class SpotSceneWriter
    implements NestableWriter
{
    /** The outer element used to enclose our spot scene definition. */
    public static final String OUTER_ELEMENT = "spot";

    // documentation inherited from interface
    public void write (Object object, DataWriter writer)
        throws SAXException
    {
        SpotSceneModel model = (SpotSceneModel)object;
        AttributesImpl attrs = new AttributesImpl();
        addSceneAttributes(model, attrs);
        writer.startElement("", OUTER_ELEMENT, "", attrs);
        writeSceneData(model, writer);
        writer.endElement(OUTER_ELEMENT);
    }

    protected void addSceneAttributes (SpotSceneModel model,
                                       AttributesImpl attrs)
    {
        if (model.defaultEntranceId != -1) {
            attrs.addAttribute("", "defaultEntranceId", "", "",
                               String.valueOf(model.defaultEntranceId));
        }
    }

    protected void writeSceneData (SpotSceneModel model, DataWriter writer)
        throws SAXException
    {
        // write out the portal info
        for (int ii = 0; ii < model.portals.length; ii++) {
            EditablePortal port = (EditablePortal)model.portals[ii];
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("", "portalId", "", "",
                               String.valueOf(port.portalId));
            addPortalLocationAttributes(port.loc, attrs);
            maybeAddAttr(attrs, "name", port.name);
            maybeAddAttr(attrs, "targetSceneName", port.targetSceneName);
            maybeAddAttr(attrs, "targetPortalName", port.targetPortalName);
            writer.emptyElement("", "portal", "", attrs);
        }
    }

    protected void addPortalLocationAttributes (
            Location portalLoc, AttributesImpl attrs)
    {
        // we assume here that the Location is purely 2d. Subclasses
        // may write out more information
        attrs.addAttribute("", "x", "", "", String.valueOf(portalLoc.x));
        attrs.addAttribute("", "y", "", "", String.valueOf(portalLoc.y));
        attrs.addAttribute("", "orient", "", "",
                           String.valueOf(portalLoc.orient));
    }

    /**
     * Adds the supplied attribute to the attributes object iff the value
     * is non-null.
     */
    protected void maybeAddAttr (
        AttributesImpl attrs, String name, String value)
    {
        if (value != null) {
            attrs.addAttribute("", name, "", "", value);
        }
    }
}
