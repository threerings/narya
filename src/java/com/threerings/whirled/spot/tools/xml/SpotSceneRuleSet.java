//
// $Id: SpotSceneRuleSet.java,v 1.6 2004/08/27 02:20:48 mdb Exp $
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

import com.samskivert.xml.SetPropertyFieldsRule;
import com.threerings.tools.xml.NestableRuleSet;
import org.apache.commons.digester.Digester;

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
        digester.addObjectCreate(prefix + "/portal",
                                 EditablePortal.class.getName());
        digester.addRule(prefix + "/portal", new SetPropertyFieldsRule());
        digester.addSetNext(prefix + "/portal", "addPortal",
                            Portal.class.getName());
    }
}
