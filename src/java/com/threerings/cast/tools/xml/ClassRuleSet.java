//
// $Id: ClassRuleSet.java,v 1.3 2004/07/13 16:34:49 mdb Exp $

package com.threerings.cast.tools.xml;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.StringUtil;
import com.samskivert.xml.SetPropertyFieldsRule;
import com.samskivert.xml.SetPropertyFieldsRule.FieldParser;

import com.threerings.cast.ComponentClass.PriorityOverride;
import com.threerings.cast.ComponentClass;
import com.threerings.util.DirectionUtil;

/**
 * The class rule set is used to parse the attributes of a component class
 * instance.
 */
public class ClassRuleSet extends RuleSetBase
{
    /** The component of the digester path that is appended by the class
     * rule set to match a component class. This is appended to whatever
     * prefix is provided to the class rule set to obtain the complete XML
     * path to a matched class. */
    public static final String CLASS_PATH = "/class";

    /**
     * Instructs the class rule set to match component classes with the
     * supplied prefix. For example, passing a prefix of
     * <code>classes</code> will match classes in the following XML file:
     *
     * <pre>
     * &lt;classes&gt;
     *   &lt;class .../&gt;
     * &lt;/classes&gt;
     * </pre>
     *
     * This must be called before adding the ruleset to a digester.
     */
    public void setPrefix (String prefix)
    {
        _prefix = prefix;
    }

    /**
     * Adds the necessary rules to the digester to parse our classes.
     */
    public void addRuleInstances (Digester digester)
    {
        // this creates the appropriate instance when we encounter a
        // <class> tag
        digester.addObjectCreate(_prefix + CLASS_PATH,
                                 ComponentClass.class.getName());

        // grab the attributes from the <class> tag
        digester.addRule(_prefix + CLASS_PATH, new SetPropertyFieldsRule());

        // parse render priority overrides
        String opath = _prefix + CLASS_PATH + "/override";
        digester.addObjectCreate(opath, PriorityOverride.class.getName());
        SetPropertyFieldsRule rule = new SetPropertyFieldsRule();
        rule.addFieldParser("orients", new FieldParser() {
            public Object parse (String text) {
                String[] orients = StringUtil.parseStringArray(text);
                ArrayIntSet oset = new ArrayIntSet();
                for (int ii = 0; ii < orients.length; ii++) {
                    oset.add(DirectionUtil.fromShortString(orients[ii]));
                }
                return oset;
            }
        });
        digester.addRule(opath, rule);

        digester.addSetNext(opath, "addPriorityOverride",
                            PriorityOverride.class.getName());
    }

    /** The prefix at which me match our component classes. */
    protected String _prefix;
}
