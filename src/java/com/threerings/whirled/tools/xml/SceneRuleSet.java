//
// $Id: SceneRuleSet.java,v 1.3 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.tools.xml;

import org.apache.commons.digester.Digester;

import com.samskivert.util.StringUtil;
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
        digester.addRule(prefix, new SetPropertyFieldsRule(digester));
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
