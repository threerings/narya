//
// $Id: FringeConfigurationParser.java,v 1.2 2002/04/04 04:06:57 ray Exp $

package com.threerings.miso.scene.tools.xml;

import java.io.Serializable;

import org.xml.sax.Attributes;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;

import com.samskivert.util.StringUtil;
import com.samskivert.xml.SetPropertyFieldsRule;

import com.threerings.tools.xml.CompiledConfigParser;

import com.threerings.miso.Log;
import com.threerings.miso.scene.FringeConfiguration;
import com.threerings.miso.scene.FringeConfiguration.FringeRecord;
import com.threerings.miso.scene.FringeConfiguration.FringeTileSetRecord;

import com.threerings.media.tile.TileSetIDBroker;

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
        digest.addRule(prefix, new Rule(digest) {
            // parse the bits
            public void begin (Attributes attrs)
            throws Exception
            {
                FringeConfiguration fc = (FringeConfiguration) digester.peek();

                for (int ii=0; ii < attrs.getLength(); ii++) {
                    String name = attrs.getLocalName(ii);
                    if (StringUtil.blank(name)) {
                        name = attrs.getQName(ii);
                    }
                    String value = attrs.getValue(ii);

                    if ("tiles".equals(name)) {
                        fc.setTiles(StringUtil.parseIntArray(value));
                    }
                }
            }
        });

        // create and configure fringe config instances
        prefix += "/base";
        digest.addObjectCreate(prefix, FringeRecord.class.getName());
        digest.addSetNext(
            prefix, "addFringeRecord", FringeRecord.class.getName());
        digest.addRule(prefix, new Rule(digest) {
            // parse the fringe record, converting tileset names to tileset
            // ids
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
                    }
                }
            }
        });

        // create the tileset records in each fringe record
        prefix += "/tileset";
        digest.addObjectCreate(prefix, FringeTileSetRecord.class.getName());
        digest.addSetNext(
            prefix, "addTileset", FringeTileSetRecord.class.getName());
        digest.addRule(prefix, new Rule(digest) {
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
                        f.mask = Boolean.getBoolean(value);
                    }
                }
            }
        });
    }

    protected TileSetIDBroker _idBroker;
}
