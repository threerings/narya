//
// $Id: MisoSceneRuleSet.java,v 1.12 2003/01/31 23:10:46 mdb Exp $

package com.threerings.miso.tools.xml;

import java.util.ArrayList;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

import com.samskivert.xml.CallMethodSpecialRule;
import com.samskivert.xml.SetFieldRule;
import com.samskivert.xml.SetPropertyFieldsRule;

import com.threerings.miso.Log;
import com.threerings.miso.data.MisoSceneModel;
import com.threerings.miso.data.ObjectInfo;

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
        digester.addRule(_prefix + "/viewwidth",
                         new SetFieldRule(digester, "vwidth"));
        digester.addRule(_prefix + "/viewheight",
                         new SetFieldRule(digester, "vheight"));
        digester.addRule(_prefix + "/base",
                         new SetFieldRule(digester, "baseTileIds"));

        digester.addObjectCreate(_prefix + "/objects",
                                 ArrayList.class.getName());
        digester.addObjectCreate(_prefix + "/objects/object",
                                 ObjectInfo.class.getName());
        digester.addSetNext(_prefix + "/objects/object", "add",
                            Object.class.getName());

        digester.addRule(_prefix + "/objects/object",
                         new SetPropertyFieldsRule(digester));

        digester.addRule(_prefix + "/objects", new CallMethodSpecialRule(
                             digester) {
            public void parseAndSet (String bodyText, Object target)
                throws Exception
            {
                ArrayList ilist = (ArrayList)target;
                ArrayList ulist = new ArrayList();
                MisoSceneModel model = (MisoSceneModel)this.digester.peek(1);

                // filter interesting and uninteresting into two lists
                for (int ii = 0; ii < ilist.size(); ii++) {
                    ObjectInfo info = (ObjectInfo)ilist.get(ii);
                    if (!info.isInteresting()) {
                        ilist.remove(ii--);
                        ulist.add(info);
                    }
                }

                // now populate the model
                MisoSceneModel.populateObjects(model, ilist, ulist);
            }
        });
    }

    /** The prefix at which me match our scenes. */
    protected String _prefix;
}
