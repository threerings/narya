//
// $Id: ClassRuleSet.java,v 1.1 2001/11/27 08:09:35 mdb Exp $

package com.threerings.cast.tools.xml;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

import com.samskivert.xml.SetPropertyFieldsRule;

import com.threerings.cast.ComponentClass;

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
        digester.addRule(_prefix + CLASS_PATH,
                         new SetPropertyFieldsRule(digester));
    }

    /** The prefix at which me match our component classes. */
    protected String _prefix;
}
