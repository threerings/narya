//
// $Id: ActionRuleSet.java,v 1.2 2002/06/26 23:53:07 mdb Exp $

package com.threerings.cast.tools.xml;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

import com.samskivert.util.StringUtil;

import com.samskivert.xml.CallMethodSpecialRule;
import com.samskivert.xml.SetFieldRule;
import com.samskivert.xml.SetPropertyFieldsRule;

import com.threerings.util.DirectionCodes;
import com.threerings.util.DirectionUtil;

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

        CallMethodSpecialRule orient = new CallMethodSpecialRule(digester) {
            public void parseAndSet (String bodyText, Object target)
                throws Exception {
                ActionSequence seq = ((ActionSequence)target);
                String[] ostrs = StringUtil.parseStringArray(bodyText);
                seq.orients = new int[ostrs.length];
                for (int ii = 0; ii < ostrs.length; ii++) {
                    int orient = DirectionUtil.fromShortString(ostrs[ii]);
                    if (orient != DirectionCodes.NONE) {
                        seq.orients[ii] = orient;
                    } else {
                        String errmsg = "Invalid orientation specification " +
                            "[index=" + ii + ", orient=" + ostrs[ii] + "].";
                        throw new Exception(errmsg);
                    }
                }
            }
        };
        digester.addRule(_prefix + ACTION_PATH + "/orients", orient);
    }

    /**
     * Validates that all necessary fields have been parsed and set in
     * this action sequence object and are valid.
     *
     * @return null if the sequence is valid, a string explaining the
     * invalidity if it is not.
     */
    public static String validate (ActionSequence seq)
    {
        if (StringUtil.blank(seq.name)) {
            return "Missing 'name' definition.";
        }
        if (seq.framesPerSecond == 0) {
            return "Missing 'framesPerSecond' definition.";
        }
        if (seq.orients == null) {
            return "Missing 'orients' definition.";
        }
        return null;
    }

    /** The prefix at which me match our actions. */
    protected String _prefix;
}
