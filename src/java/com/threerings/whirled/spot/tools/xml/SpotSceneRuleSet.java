//
// $Id: SpotSceneRuleSet.java,v 1.5 2004/07/13 16:34:49 mdb Exp $

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
