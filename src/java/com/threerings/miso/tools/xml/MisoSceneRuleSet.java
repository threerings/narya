//
// $Id: MisoSceneRuleSet.java,v 1.2 2001/11/18 04:27:56 mdb Exp $

package com.threerings.media.tile.xml;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

import com.samskivert.xml.SetFieldRule;

import com.threerings.miso.scene.MisoSceneModel;

/**
 * Used to parse a {@link MisoSceneModel} from XML.
 */
public class MisoSceneRuleSet extends RuleSetBase
{
    /**
     * Constructs a miso scene rule set which will match scenes with the
     * supplied prefix. For example, passing <code>scene.miso</code> will
     * match the scene in the following XML file:
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
    public MisoSceneRuleSet (String prefix)
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
        digester.addRule(_prefix + "/fringe",
                         new SetFieldRule(digester, "fringeTileIds"));
        digester.addRule(_prefix + "/object",
                         new SetFieldRule(digester, "objectTileIds"));
    }

    /** The prefix at which me match our scenes. */
    protected String _prefix;
}
