//
// $Id: SpotSceneRuleSet.java,v 1.1 2001/11/29 19:31:52 mdb Exp $

package com.threerings.whirled.tools.spot.xml;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

import com.samskivert.xml.SetFieldRule;

import com.threerings.whirled.spot.data.SpotSceneModel;
import com.threerings.whirled.tools.xml.SceneRuleSet;

/**
 * Used to parse a {@link SpotSceneModel} from XML.
 */
public class SpotSceneRuleSet extends SceneRuleSet
{
    /**
     * Adds the necessary rules to the digester to parse our miso scene
     * data.
     */
    public void addRuleInstances (Digester digester)
    {
        super.addRuleInstances(digester);

        // add extra rules for the spot scene model fields
        digester.addRule(_prefix + "/locationIds",
                         new SetFieldRule(digester, "locationIds"));
        digester.addRule(_prefix + "/locationX",
                         new SetFieldRule(digester, "locationX"));
        digester.addRule(_prefix + "/locationY",
                         new SetFieldRule(digester, "locationY"));
        digester.addRule(_prefix + "/locationOrients",
                         new SetFieldRule(digester, "locationOrients"));
        digester.addRule(_prefix + "/locationClusters",
                         new SetFieldRule(digester, "locationClusters"));
        digester.addRule(_prefix + "/defaultEntranceId",
                         new SetFieldRule(digester, "defaultEntranceId"));
        digester.addRule(_prefix + "/portalIds",
                         new SetFieldRule(digester, "portalIds"));
        digester.addRule(_prefix + "/targetLocIds",
                         new SetFieldRule(digester, "targetLocIds"));
    }

    // documentation inherited
    protected Class getSceneModelClass ()
    {
        return SpotSceneModel.class;
    }
}
