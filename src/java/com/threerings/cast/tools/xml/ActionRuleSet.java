//
// $Id: ActionRuleSet.java,v 1.1 2001/11/27 08:09:35 mdb Exp $

package com.threerings.cast.tools.xml;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

import com.samskivert.util.StringUtil;

import com.samskivert.xml.CallMethodSpecialRule;
import com.samskivert.xml.SetFieldRule;
import com.samskivert.xml.SetPropertyFieldsRule;

import com.threerings.cast.ActionSequence;

/**
 * The action rule set is used to parse the attributes of an action
 * sequence instance.
 */
public class ActionRuleSet extends RuleSetBase
{
    /** The component of the digester path that is appended by the action
     * rule set to match a action. This is appended to whatever prefix is
     * provided to the action rule set to obtain the complete XML path to
     * a matched action. */
    public static final String ACTION_PATH = "/action";

    /**
     * Instructs the action rule set to match actions with the supplied
     * prefix. For example, passing a prefix of <code>actions</code> will
     * match actions in the following XML file:
     *
     * <pre>
     * &lt;actions&gt;
     *   &lt;action&gt;
     *     // ...
     *   &lt;/action&gt;
     * &lt;/actions&gt;
     * </pre>
     *
     * This must be called before adding the ruleset to a digester.
     */
    public void setPrefix (String prefix)
    {
        _prefix = prefix;
    }

    /**
     * Adds the necessary rules to the digester to parse our actions.
     */
    public void addRuleInstances (Digester digester)
    {
        // this creates the appropriate instance when we encounter a
        // <action> tag
        digester.addObjectCreate(_prefix + ACTION_PATH,
                                 ActionSequence.class.getName());

        // grab the name attribute from the <action> tag
        digester.addRule(_prefix + ACTION_PATH,
                         new SetPropertyFieldsRule(digester));

        // grab the other attributes from their respective tags
        digester.addRule(_prefix + ACTION_PATH + "/framesPerSecond",
                         new SetFieldRule(digester, "framesPerSecond"));

        CallMethodSpecialRule origin = new CallMethodSpecialRule(digester) {
            public void parseAndSet (String bodyText, Object target)
                throws Exception {
                int[] coords = StringUtil.parseIntArray(bodyText);
                if (coords.length != 2) {
                    String errmsg = "Invalid <origin> specification '" +
                        bodyText + "'.";
                    throw new Exception(errmsg);
                }
                ((ActionSequence)target).origin.setLocation(
                    coords[0], coords[1]);
            }
        };
        digester.addRule(_prefix + ACTION_PATH + "/origin", origin);
    }

    /** The prefix at which me match our actions. */
    protected String _prefix;
}
