//
// $Id: ColorPositoryParser.java,v 1.1 2003/01/31 23:10:45 mdb Exp $

package com.threerings.media.image.tools.xml;

import java.io.Serializable;

import org.xml.sax.Attributes;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import com.samskivert.xml.SetPropertyFieldsRule;

import com.threerings.tools.xml.CompiledConfigParser;

import com.threerings.media.image.ColorPository.ClassRecord;
import com.threerings.media.image.ColorPository.ColorRecord;
import com.threerings.media.image.ColorPository;

/**
 * Parses the XML color repository definition and creates a {@link
 * ColorPository} instance that reflects its contents.
 */
public class ColorPositoryParser extends CompiledConfigParser
{
    // documentation inherited
    protected Serializable createConfigObject ()
    {
        return new ColorPository();
    }

    // documentation inherited
    protected void addRules (Digester digest)
    {
        // create and configure class record instances
        String prefix = "colors/class";
        digest.addObjectCreate(prefix, ClassRecord.class.getName());
        digest.addRule(prefix, new SetPropertyFieldsRule(digest));
        digest.addSetNext(prefix, "addClass", ClassRecord.class.getName());

        // create and configure color record instances
        prefix += "/color";
        digest.addRule(prefix, new Rule(digest) {
            public void begin (Attributes attributes) throws Exception {
                // we want to inherit settings from the color class when
                // creating the record, so we do some custom stuff
                ColorRecord record = new ColorRecord();
                ClassRecord clrec = (ClassRecord)digester.peek();
                record.starter = clrec.starter;
                digester.push(record);
            }

            public void end () throws Exception {
                digester.pop();
            }
        });
        digest.addRule(prefix, new SetPropertyFieldsRule(digest));
        digest.addSetNext(prefix, "addColor", ColorRecord.class.getName());
    }
}
