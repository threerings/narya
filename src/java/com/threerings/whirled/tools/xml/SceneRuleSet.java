//
// $Id: SceneRuleSet.java,v 1.6 2004/08/27 02:20:48 mdb Exp $
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

package com.threerings.whirled.tools.xml;

import org.apache.commons.digester.Digester;

import com.samskivert.xml.SetPropertyFieldsRule;

import com.threerings.tools.xml.NestableRuleSet;
import com.threerings.whirled.data.SceneModel;

/**
 * Used to parse a {@link SceneModel} from XML.
 */
public class SceneRuleSet implements NestableRuleSet
{
    // documentation inherited from interface
    public String getOuterElement ()
    {
        return SceneWriter.OUTER_ELEMENT;
    }

    // documentation inherited from interface
    public void addRuleInstances (String prefix, Digester digester)
    {
        // this creates the appropriate instance when we encounter our tag
        digester.addObjectCreate(prefix, getSceneClass().getName());

        // set up rules to parse and set our fields
        digester.addRule(prefix, new SetPropertyFieldsRule());
    }

    /**
     * This indicates the class (which should extend {@link SceneModel})
     * to be instantiated during the parsing process.
     */
    protected Class getSceneClass ()
    {
        return SceneModel.class;
    }
}
