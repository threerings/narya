//
// $Id: StageSceneRuleSet.java 6370 2003-02-12 07:32:00Z mdb $

package com.threerings.stage.tools.xml;

import com.threerings.whirled.tools.xml.SceneRuleSet;

import com.threerings.stage.data.StageScene;
import com.threerings.stage.data.StageSceneModel;

/**
 * Used to parse an {@link StageScene} from XML.
 */
public class StageSceneRuleSet extends SceneRuleSet
{
    // documentation inherited
    protected Class getSceneClass ()
    {
        return StageSceneModel.class;
    }
}
