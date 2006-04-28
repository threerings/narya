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

import java.lang.reflect.Field;

import com.samskivert.xml.SetPropertyFieldsRule;
import com.samskivert.util.StringUtil;
import com.samskivert.util.ValueMarshaller;

import com.threerings.tools.xml.NestableRuleSet;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreateRule;
import org.apache.commons.digester.Rule;

import org.xml.sax.Attributes;

import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SpotSceneModel;
import com.threerings.whirled.spot.tools.EditablePortal;

/**
 * Used to parse a {@link SpotSceneModel} from XML.
 */
public class SpotSceneRuleSet implements NestableRuleSet
{
    // documentation inherited from interface
    public String getOuterElement ()
    {
        return SpotSceneWriter.OUTER_ELEMENT;
    }

    // documentation inherited from interface
    public void addRuleInstances (String prefix, Digester digester)
    {
        digester.addObjectCreate(prefix, SpotSceneModel.class.getName());

        // set up rules to parse and set our fields
        digester.addRule(prefix, new SetPropertyFieldsRule());

        // create EditablePortal instances when we see <portal>
        digester.addRule(prefix + "/portal", new PortalCreateRule(this));
        digester.addRule(prefix + "/portal", new PortalFieldsRule());
        digester.addSetNext(prefix + "/portal", "addPortal",
                            Portal.class.getName());
    }

    /**
     * Create a new instance of the Location class that should be used
     * with Portals.
     */
    protected Location createNewLocation ()
    {
        return new Location();
    }

    /**
     * A rule used to create the portal but also initialize the Location
     * property within it.
     */
    protected static class PortalCreateRule extends ObjectCreateRule
    {
        public PortalCreateRule (SpotSceneRuleSet ruleset)
        {
            super(EditablePortal.class.getName());
            _ruleset = ruleset;
        }

        // documentation inherited
        public void begin (String namespace, String name, Attributes attributes)
                throws Exception
        {
            super.begin(namespace, name, attributes);

            // create the empty Location in the Portal
            Portal p = (Portal) digester.peek();
            p.loc = _ruleset.createNewLocation();
        }

        protected SpotSceneRuleSet _ruleset;
    }

    /**
     * Set fields in the Portal, or in the Location object
     * contained therein. If there are ambiguous attribute names then..
     * well. yeah.
     */
    protected static class PortalFieldsRule extends Rule
    {
        // documentation inherited
        public void begin (String namespace, String name, Attributes attrs)
            throws Exception
        {
            Portal portal = (Portal) digester.peek();
            Class portalClass = portal.getClass();
            Location loc = portal.loc;
            Class locClass = loc.getClass();

            // iterate over the attributes, setting public fields where
            // applicable
            for (int i = 0; i < attrs.getLength(); i++) {
                String lname = attrs.getLocalName(i);
                if (StringUtil.isBlank(lname)) {
                    lname = attrs.getQName(i);
                }

                // look for a public field with this lname
                Field field;
                Object container;
                try {
                    field = portalClass.getField(lname);
                    container = portal;

                } catch (NoSuchFieldException nsfe) {
                    // if we didn't find the field in the Portal, maybe it's
                    // in the Location
                    try {
                        field = locClass.getField(lname);
                        container = loc;

                    } catch (NoSuchFieldException nsfe2) {
                        digester.getLogger().warn(
                            "Skipping property '" + lname +
                            "' for which there is no field.");
                        continue;
                    }
                }

                // convert the value into the appropriate object type
                String valstr = attrs.getValue(i);
                // use the value marshaller to parse the
                // property based on the type of the target object field
                Object value = ValueMarshaller.unmarshal(
                    field.getType(), valstr);

                if (digester.getLogger().isDebugEnabled()) {
                    digester.getLogger().debug("  Setting property '" + lname +
                                               "' to '" + valstr + "'");
                }

                // and finally set the field
                field.set(container, value);
            }
        }
    }
}
