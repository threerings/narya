//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.cast.tools.xml;

import java.awt.Color;

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
        SetPropertyFieldsRule rule = new SetPropertyFieldsRule();
        rule.addFieldParser("shadowColor", new FieldParser() {
            public Object parse (String text) {
                int[] values = StringUtil.parseIntArray(text);
                return new Color(values[0], values[1], values[2], values[3]);
            }
        });
        digester.addRule(_prefix + CLASS_PATH, rule);

        // parse render priority overrides
        String opath = _prefix + CLASS_PATH + "/override";
        digester.addObjectCreate(opath, PriorityOverride.class.getName());
        rule = new SetPropertyFieldsRule();
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
