//
// $Id: SpotSceneRuleSet.java,v 1.2 2001/12/05 03:38:09 mdb Exp $

package com.threerings.whirled.tools.spot.xml;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

import com.samskivert.xml.SetPropertyFieldsRule;

import com.threerings.whirled.tools.xml.SceneRuleSet;

import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.tools.spot.EditablePortal;
import com.threerings.whirled.tools.spot.EditableSpotScene;
import com.threerings.whirled.tools.spot.EditableSpotSceneImpl;

/**
 * Used to parse an {@link EditableSpotScene} from XML.
 */
public class SpotSceneRuleSet extends SceneRuleSet
{
    /**
     * Extends the scene rule set with the necessary rules to parse our
     * spot scene data.
     */
    public void addRuleInstances (Digester digester)
    {
        super.addRuleInstances(digester);

        // create Location instances when we see <location>
        String elclass = Location.class.getName();
        digester.addObjectCreate(_prefix + "/location", elclass);
        digester.addRule(_prefix + "/location",
                         new SetPropertyFieldsRule(digester));
        digester.addSetNext(_prefix + "/location", "addLocation", elclass);

        // create EditablePortal instances when we see <portal>
        String epclass = EditablePortal.class.getName();
        digester.addObjectCreate(_prefix + "/portal", epclass);
        digester.addRule(_prefix + "/portal",
                         new SetPropertyFieldsRule(digester));
        digester.addSetNext(_prefix + "/portal", "addPortal", epclass);
    }

    // documentation inherited
    protected Class getSceneClass ()
    {
        return EditableSpotSceneImpl.class;
    }
}
