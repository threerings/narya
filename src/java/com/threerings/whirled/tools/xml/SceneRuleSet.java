//
// $Id: SceneRuleSet.java,v 1.2 2001/12/05 03:38:09 mdb Exp $

package com.threerings.whirled.tools.xml;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

import com.threerings.whirled.tools.EditableScene;
import com.threerings.whirled.tools.EditableSceneImpl;

/**
 * Used to parse an {@link EditableScene} from XML.
 */
public class SceneRuleSet extends RuleSetBase
{
    /**
     * Configures this scene rule set to match scenes with the supplied
     * prefix. For example, passing <code>scene</code> will match the
     * scene in the following XML file:
     *
     * <pre>
     * &lt;scene name="Scene Name" version="3"&gt;
     *   &lt;neighbor&gt;North Scene&lt;/neighbor&gt;
     *   &lt;neighbor&gt;West Scene&lt;/neighbor&gt;
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
        // this creates the appropriate instance when we encounter our tag
        digester.addObjectCreate(_prefix, getSceneClass().getName());

        // set up rules to parse and set our fields
        digester.addSetProperties(_prefix);
        digester.addCallMethod(_prefix + "/neighbor", "addNeighbor", 0);
    }

    /**
     * This indicates the class (which should implement {@link
     * EditableScene}) to be instantiated during the parsing process.
     */
    protected Class getSceneClass ()
    {
        return EditableSceneImpl.class;
    }

    /** The prefix at which me match our scenes. */
    protected String _prefix = DEFAULT_SCENE_PREFIX;

    /** The default prefix which matches &lt;scene&gt;. */
    protected static final String DEFAULT_SCENE_PREFIX = "scene";
}
