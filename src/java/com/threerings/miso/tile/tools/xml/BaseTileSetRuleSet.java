//
// $Id: BaseTileSetRuleSet.java,v 1.7 2001/11/29 21:58:49 mdb Exp $

package com.threerings.miso.tools.tile.xml;

import org.xml.sax.Attributes;
import org.apache.commons.digester.Digester;

import com.samskivert.util.StringUtil;
import com.samskivert.xml.CallMethodSpecialRule;

import com.threerings.media.tile.TileSet;
import com.threerings.media.tools.tile.xml.SwissArmyTileSetRuleSet;

import com.threerings.miso.Log;
import com.threerings.miso.tile.BaseTileSet;

/**
 * Parses {@link BaseTileSet} instances from a tileset description. Base
 * tilesets extend swiss army tilesets with the addition of a passability
 * flag for each tile.
 *
 * @see SwissArmyTileSetRuleSet
 */
public class BaseTileSetRuleSet extends SwissArmyTileSetRuleSet
{
    // documentation inherited
    public void addRuleInstances (Digester digester)
    {
        super.addRuleInstances(digester);

        digester.addRule(
            _prefix + TILESET_PATH + "/passable",
            new CallMethodSpecialRule(digester) {
                public void parseAndSet (String bodyText, Object target)
                {
                    int[] values = StringUtil.parseIntArray(bodyText);
                    boolean[] passable = new boolean[values.length];
                    for (int i = 0; i < values.length; i++) {
                        passable[i] = (values[i] != 0);
                    }
                    BaseTileSet starget = (BaseTileSet)target;
                    starget.setPassability(passable);
                }
            });
    }

    // documentation inherited
    public boolean isValid (Object target)
    {
        BaseTileSet set = (BaseTileSet)target;
        boolean valid = super.isValid(target);

        // check for a <passable> element
        if (set.getPassability() == null) {
            Log.warning("Tile set definition missing valid <passable> " +
                        "element [set=" + set + "].");
            valid = false;
        }

        return valid;
    }

    // documentation inherited
    protected Class getTileSetClass ()
    {
        return BaseTileSet.class;
    }
}
