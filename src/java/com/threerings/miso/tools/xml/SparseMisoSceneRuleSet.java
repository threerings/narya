//
// $Id: SparseMisoSceneRuleSet.java,v 1.2 2003/04/21 17:08:57 mdb Exp $

package com.threerings.miso.tools.xml;

import java.util.ArrayList;
import org.xml.sax.Attributes;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;

import com.samskivert.xml.CallMethodSpecialRule;
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
        dig.addRule(prefix, new Rule(dig) {
            public void begin (Attributes attributes) throws Exception {
                digester.push(createMisoSceneModel());
            }
            public void end () throws Exception {
                digester.pop();
            }
        });

        // set up rules to parse and set our fields
        dig.addRule(prefix + "/swidth", new SetFieldRule(dig, "swidth"));
        dig.addRule(prefix + "/sheight", new SetFieldRule(dig, "sheight"));
        dig.addRule(prefix + "/defTileSet",
                    new SetFieldRule(dig, "defTileSet"));

        String sprefix = prefix + "/sections/section";
        dig.addObjectCreate(sprefix, Section.class.getName());
        dig.addRule(sprefix, new SetPropertyFieldsRule(dig));
        dig.addRule(sprefix + "/base", new SetFieldRule(dig, "baseTileIds"));
        dig.addObjectCreate(sprefix + "/objects/object",
                            ObjectInfo.class.getName());
        dig.addRule(sprefix + "/objects/object",
                    new SetPropertyFieldsRule(dig));
        dig.addSetNext(sprefix + "/objects/object", "addObject",
                       ObjectInfo.class.getName());
        dig.addSetNext(sprefix, "setSection", Section.class.getName());
    }

    protected SparseMisoSceneModel createMisoSceneModel ()
    {
        return new SparseMisoSceneModel();
    }
}
