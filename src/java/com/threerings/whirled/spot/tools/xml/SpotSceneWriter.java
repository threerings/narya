//
// $Id: SpotSceneWriter.java,v 1.4 2001/12/05 08:45:06 mdb Exp $

package com.threerings.whirled.tools.spot.xml;

import java.util.Iterator;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.megginson.sax.DataWriter;
import com.samskivert.util.StringUtil;

import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;

import com.threerings.whirled.tools.EditableScene;
import com.threerings.whirled.tools.spot.EditablePortal;
import com.threerings.whirled.tools.spot.EditableSpotScene;
import com.threerings.whirled.tools.xml.SceneWriter;

/**
 * Generates an XML representation of an {@link EditableSpotScene}.
 */
public class SpotSceneWriter extends SceneWriter
{
    protected void addSceneAttributes (
        EditableScene scene, AttributesImpl attrs)
    {
        super.addSceneAttributes(scene, attrs);
        EditableSpotScene sscene = (EditableSpotScene)scene;
        attrs.addAttribute("", "defaultEntranceId", "", "",
                           Integer.toString(sscene.getDefaultEntranceId()));
    }

    protected void writeSceneData (EditableScene scene, DataWriter writer)
        throws SAXException
    {
        // we don't want to write our superclass scene data because it
        // writes out neighbors info which we deal with differently, so we
        // mean not to call super.writeSceneData()

        EditableSpotScene sscene = (EditableSpotScene)scene;

        // write out the location info
        Iterator iter = sscene.getLocations().iterator();
        while (iter.hasNext()) {
            Location loc = (Location)iter.next();
            // skip portals as we'll get those on the next run
            if (loc instanceof EditablePortal) {
                continue;
            }

            AttributesImpl attrs = new AttributesImpl();
            addSharedAttrs(attrs, loc);
            attrs.addAttribute("", "clusterIndex", "", "",
                               Integer.toString(loc.clusterIndex));
            writer.emptyElement("", "location", "", attrs);
        }

        // write out the portal info
        iter = sscene.getPortals().iterator();
        while (iter.hasNext()) {
            EditablePortal port = (EditablePortal)iter.next();
            AttributesImpl attrs = new AttributesImpl();
            addSharedAttrs(attrs, port);
            attrs.addAttribute("", "targetSceneName", "", "",
                               port.targetSceneName);
            attrs.addAttribute("", "targetPortalName", "", "",
                               port.targetPortalName);
            writer.emptyElement("", "location", "", attrs);
        }
    }

    /**
     * Adds the attributes that are shared between location and portal
     * elements.
     */
    protected void addSharedAttrs (AttributesImpl attrs, Location loc)
    {
        attrs.addAttribute("", "locationId", "", "",
                           Integer.toString(loc.locationId));
        attrs.addAttribute("", "x", "", "", Integer.toString(loc.x));
        attrs.addAttribute("", "y", "", "", Integer.toString(loc.y));
        attrs.addAttribute("", "orientation", "", "",
                           Integer.toString(loc.orientation));
    }
}
