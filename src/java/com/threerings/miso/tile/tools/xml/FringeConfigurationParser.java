//
// $Id: FringeConfigurationParser.java,v 1.10 2004/07/13 16:34:49 mdb Exp $

package com.threerings.miso.tile.tools.xml;

import java.io.Serializable;

import org.xml.sax.Attributes;
import org.apache.commons.digester.Digester;

import com.samskivert.util.StringUtil;
import com.samskivert.xml.SetPropertyFieldsRule;
import com.samskivert.xml.ValidatedSetNextRule;

import com.threerings.tools.xml.CompiledConfigParser;

import com.threerings.media.tile.TileSetIDBroker;

import com.threerings.miso.Log;
import com.threerings.miso.tile.FringeConfiguration.FringeRecord;
import com.threerings.miso.tile.FringeConfiguration.FringeTileSetRecord;
import com.threerings.miso.tile.FringeConfiguration;

/**
 * Parses fringe config definitions.
 */
public class FringeConfigurationParser extends CompiledConfigParser
{
    public FringeConfigurationParser (TileSetIDBroker broker)
    {
        _idBroker = broker;
    }


    // documentation inherited
    protected Serializable createConfigObject ()
    {
        return new FringeConfiguration();
    }

    // documentation inherited
    protected void addRules (Digester digest)
    {
        // configure top-level constraints
        String prefix = "fringe";
        digest.addRule(prefix, new SetPropertyFieldsRule());

        // create and configure fringe config instances
        prefix += "/base";
        digest.addObjectCreate(prefix, FringeRecord.class.getName());

        ValidatedSetNextRule.Validator val;
        val = new ValidatedSetNextRule.Validator() {
            public boolean isValid (Object target) {
                if (((FringeRecord) target).isValid()) {
                    return true;
                } else {
                    Log.warning("A FringeRecord was not added because it was " +
                                "improperly specified [rec=" + target + "].");
                    return false;
                }
            }
        };
        ValidatedSetNextRule vrule;
        vrule = new ValidatedSetNextRule("addFringeRecord", val) {
            // parse the fringe record, converting tileset names to
            // tileset ids
            public void begin (Attributes attrs)
                throws Exception
            {
                FringeRecord frec = (FringeRecord) digester.peek();

                for (int ii=0; ii < attrs.getLength(); ii++) {
                    String name = attrs.getLocalName(ii);
                    if (StringUtil.blank(name)) {
                        name = attrs.getQName(ii);
                    }
                    String value = attrs.getValue(ii);

                    if ("name".equals(name)) {
                        if (_idBroker.tileSetMapped(value)) {
                            frec.base_tsid = _idBroker.getTileSetID(value);
                        } else {
                            Log.warning("Skipping unknown base " +
                                "tileset [name=" + value + "].");
                        }

                    } else if ("priority".equals(name)) {
                        frec.priority = Integer.parseInt(value);
                    } else {
                        Log.warning("Skipping unknown attribute " +
                                    "[name=" + name + "].");
                    }
                }
            }
        };
        digest.addRule(prefix, vrule);

        // create the tileset records in each fringe record
        prefix += "/tileset";
        digest.addObjectCreate(prefix, FringeTileSetRecord.class.getName());

        val = new ValidatedSetNextRule.Validator() {
            public boolean isValid (Object target) {
                if (((FringeTileSetRecord) target).isValid()) {
                    return true;
                } else {
                    Log.warning("A FringeTileSetRecord was not added because " +
                                "it was improperly specified " +
                                "[rec=" + target + "].");
                    return false;
                }
            }
        };
        vrule = new ValidatedSetNextRule("addTileset", val) {
            // parse the fringe tilesetrecord, converting tileset names to ids
            public void begin (Attributes attrs)
                throws Exception
            {
                FringeTileSetRecord f = (FringeTileSetRecord) digester.peek();

                for (int ii=0; ii < attrs.getLength(); ii++) {
                    String name = attrs.getLocalName(ii);
                    if (StringUtil.blank(name)) {
                        name = attrs.getQName(ii);
                    }
                    String value = attrs.getValue(ii);

                    if ("name".equals(name)) {
                        if (_idBroker.tileSetMapped(value)) {
                            f.fringe_tsid = _idBroker.getTileSetID(value);
                        } else {
                            Log.warning("Skipping unknown fringe " +
                                "tileset [name=" + value + "].");
                        }

                    } else if ("mask".equals(name)) {
                        f.mask = Boolean.valueOf(value).booleanValue();
                    } else {
                        Log.warning("Skipping unknown attribute " +
                                    "[name=" + name + "].");
                    }
                }
            }
        };
        digest.addRule(prefix, vrule);
    }

    protected TileSetIDBroker _idBroker;
}
