//
// $Id: SceneRuleSet.java,v 1.1 2001/11/29 19:31:52 mdb Exp $

package com.threerings.whirled.tools.xml;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

import com.samskivert.xml.SetFieldRule;

import com.threerings.whirled.data.SceneModel;

/**
 * Used to parse a {@link SceneModel} from XML.
 */
public class SceneRuleSet extends RuleSetBase
{
    /**
     * Configures this scene rule set to match scenes with the supplied
     * prefix. For example, passing <code>scene</code> will match the
     * scene in the following XML file:
     *
     * <pre>
     * &lt;scene&gt;
     *   &lt;sceneId&gt;50&lt;/sceneId&gt;
     *   &lt;version&gt;50&lt;/version&gt;
     *   &lt;!-- ... --&gt;
     * &lt;/scene&gt;
     * </pre>
     */
    public void setPrefix (String prefix)
    {
        _prefix = prefix;
    }

    /**
     * Adds the necessary rules to the digester to parse our miso scene
     * data.
     */
    public void addRuleInstances (Digester digester)
    {
        // this creates the appropriate instance when we encounter our
        // prefix tag
        digester.addObjectCreate(_prefix, getSceneModelClass().getName());

        // set up rules to parse and set our fields
        digester.addRule(_prefix + "/sceneId",
                         new SetFieldRule(digester, "sceneId"));
        digester.addRule(_prefix + "/version",
                         new SetFieldRule(digester, "version"));
        digester.addRule(_prefix + "/neighborIds",
                         new SetFieldRule(digester, "neighborIds"));
    }

    /**
     * This indicates the class (which should derive from {@link
     * SceneModel}) to be instantiated during the parsing process.
     */
    protected Class getSceneModelClass ()
    {
        return SceneModel.class;
    }

    /** The prefix at which me match our scenes. */
    protected String _prefix = DEFAULT_SCENE_PREFIX;

    /** The default prefix which matches &lt;scene&gt;. */
    protected static final String DEFAULT_SCENE_PREFIX = "scene";
}
