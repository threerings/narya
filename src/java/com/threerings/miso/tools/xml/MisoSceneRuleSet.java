//
// $Id: MisoSceneRuleSet.java,v 1.8 2002/05/16 02:25:19 ray Exp $

package com.threerings.miso.scene.tools.xml;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

import com.samskivert.xml.CallMethodSpecialRule;
import com.samskivert.xml.SetFieldRule;

import com.threerings.miso.scene.MisoSceneModel;

/**
 * Used to parse a {@link MisoSceneModel} from XML.
 */
public class MisoSceneRuleSet extends RuleSetBase
{
    /**
     * Configures the rule set to match scenes with the supplied prefix.
     * For example, passing <code>scene.miso</code> will match the scene
     * in the following XML file:
     *
     * <pre>
     * &lt;scene&gt;
     *   &lt;miso&gt;
     *     &lt;width&gt;50&lt;/width&gt;
     *     &lt;height&gt;50&lt;/height&gt;
     *     &lt;!-- ... --&gt;
     *   &lt;/miso&gt;
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
        digester.addObjectCreate(_prefix, MisoSceneModel.class.getName());

        // set up rules to parse and set our fields
        digester.addRule(_prefix + "/width",
                         new SetFieldRule(digester, "width"));
        digester.addRule(_prefix + "/height",
                         new SetFieldRule(digester, "height"));
        digester.addRule(_prefix + "/base",
                         new SetFieldRule(digester, "baseTileIds"));
        digester.addRule(_prefix + "/object",
                         new SetFieldRule(digester, "objectTileIds"));
        digester.addRule(_prefix + "/actions",
                         new SetFieldRule(digester, "objectActions"));

        // we have to unfuck the objectActions field in the event that
        // there's one object in the objects element which has a blank
        // action string (which the parser will parse as a zero length
        // array, when we want a length one array with a blank string)
        digester.addRule(_prefix, new CallMethodSpecialRule(digester) {
            public void parseAndSet (String bodyText, Object target)
                throws Exception
            {
                MisoSceneModel model = (MisoSceneModel)target;
                if (model.objectTileIds.length > 0 &&
                    model.objectActions.length == 0) {
                    model.objectActions = new String[1];
                }

                // and allocate the fringe tile layer
                model.fringeTileIds = new int[model.baseTileIds.length];
            }
        });
    }

    /** The prefix at which me match our scenes. */
    protected String _prefix;
}
