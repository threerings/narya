//
// $Id: StageMisoSceneRuleSet.java 20143 2005-03-30 01:12:48Z mdb $

package com.threerings.stage.tools.xml;

import com.threerings.miso.data.SparseMisoSceneModel;
import com.threerings.miso.tools.xml.SparseMisoSceneRuleSet;

import com.threerings.stage.data.StageMisoSceneModel;

/**
 * Customizes the miso scene rule set such that it creates {@link
 * StageMisoSceneModel} instances.
 */
public class StageMisoSceneRuleSet extends SparseMisoSceneRuleSet
{
    protected SparseMisoSceneModel createMisoSceneModel ()
    {
        return new StageMisoSceneModel();
    }
}
