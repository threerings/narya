//
// $Id: SparseMisoSceneRuleSet.java,v 1.4 2004/07/13 16:34:49 mdb Exp $

package com.threerings.miso.tools.xml;

import org.xml.sax.Attributes;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;

import com.samskivert.xml.SetFieldRule;
import com.samskivert.xml.SetPropertyFieldsRule;

import com.threerings.tools.xml.NestableRuleSet;

import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.data.SparseMisoSceneModel.Section;
import com.threerings.miso.data.SparseMisoSceneModel;

/**
 * Used to parse a {@link SparseMisoSceneModel} from XML.
 */
public class SparseMisoSceneRuleSet implements NestableRuleSet
{
    // documentation inherited from interface
    public String getOuterElement ()
    {
        return SparseMisoSceneWriter.OUTER_ELEMENT;
    }

    // documentation inherited from interface
    public void addRuleInstances (String prefix, Digester dig)
    {
        // this creates the appropriate instance when we encounter our
        // prefix tag
        dig.addRule(prefix, new Rule() {
            public void begin (String namespace, String name,
                               Attributes attributes) throws Exception {
                digester.push(createMisoSceneModel());
            }
            public void end (String namespace, String name) throws Exception {
                digester.pop();
            }
        });

        // set up rules to parse and set our fields
        dig.addRule(prefix + "/swidth", new SetFieldRule("swidth"));
        dig.addRule(prefix + "/sheight", new SetFieldRule("sheight"));
        dig.addRule(prefix + "/defTileSet", new SetFieldRule("defTileSet"));

        String sprefix = prefix + "/sections/section";
        dig.addObjectCreate(sprefix, Section.class.getName());
        dig.addRule(sprefix, new SetPropertyFieldsRule());
        dig.addRule(sprefix + "/base", new SetFieldRule("baseTileIds"));
        dig.addObjectCreate(sprefix + "/objects/object",
                            ObjectInfo.class.getName());
        dig.addRule(sprefix + "/objects/object", new SetPropertyFieldsRule());
        dig.addSetNext(sprefix + "/objects/object", "addObject",
                       ObjectInfo.class.getName());
        dig.addSetNext(sprefix, "setSection", Section.class.getName());
    }

    protected SparseMisoSceneModel createMisoSceneModel ()
    {
        return new SparseMisoSceneModel();
    }
}
