//
// $Id: SwissArmyTileSetRuleSet.java,v 1.9 2002/04/01 22:53:39 mdb Exp $

package com.threerings.media.tile.tools.xml;

import java.awt.Dimension;
import java.awt.Point;

import org.xml.sax.Attributes;
import org.apache.commons.digester.Digester;

import com.samskivert.util.StringUtil;
import com.samskivert.xml.CallMethodSpecialRule;

import com.threerings.media.Log;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.SwissArmyTileSet;

/**
 * Parses {@link SwissArmyTileSet} instances from a tileset description. A
 * swiss army tileset description looks like so:
 *
 * <pre>
 * &lt;tileset name="Sample Swiss Army Tileset"&gt;
 *   &lt;imagePath&gt;path/to/image.png&lt;/imagePath&gt;
 *   &lt;!-- the widths (per row) of each tile in pixels --&gt;
 *   &lt;widths&gt;64, 64, 64, 64&lt;/widths&gt;
 *   &lt;!-- the heights (per row) of each tile in pixels --&gt;
 *   &lt;heights&gt;48, 48, 48, 64&lt;/heights&gt;
 *   &lt;!-- the number of tiles in each row --&gt;
 *   &lt;tileCounts&gt;16, 5, 3, 10&lt;/tileCounts&gt;
 *   &lt;!-- the offset in pixels to the upper left tile --&gt;
 *   &lt;offsetPos&gt;8, 8&lt;/offsetPos&gt;
 *   &lt;!-- the gap between tiles in pixels --&gt;
 *   &lt;gapSize&gt;12, 12&lt;/gapSize&gt;
 * &lt;/tileset&gt;
 * </pre>
 */
public class SwissArmyTileSetRuleSet extends TileSetRuleSet
{
    // documentation inherited
    public void addRuleInstances (Digester digester)
    {
        super.addRuleInstances(digester);

        digester.addRule(
            _prefix + TILESET_PATH + "/widths",
            new CallMethodSpecialRule(digester) {
                public void parseAndSet (String bodyText, Object target)
                {
                    int[] widths = StringUtil.parseIntArray(bodyText);
                    ((SwissArmyTileSet)target).setWidths(widths);
                }
            });

        digester.addRule(
            _prefix + TILESET_PATH + "/heights",
            new CallMethodSpecialRule(digester) {
                public void parseAndSet (String bodyText, Object target)
                {
                    int[] heights = StringUtil.parseIntArray(bodyText);
                    ((SwissArmyTileSet)target).setHeights(heights);
                }
            });

        digester.addRule(
            _prefix + TILESET_PATH + "/tileCounts",
            new CallMethodSpecialRule(digester) {
                public void parseAndSet (String bodyText, Object target)
                {
                    int[] tileCounts = StringUtil.parseIntArray(bodyText);
                    ((SwissArmyTileSet)target).setTileCounts(tileCounts);
                }
            });

        digester.addRule(
            _prefix + TILESET_PATH + "/offsetPos",
            new CallMethodSpecialRule(digester) {
                public void parseAndSet (String bodyText, Object target)
                {
                    int[] values = StringUtil.parseIntArray(bodyText);
                    SwissArmyTileSet starget = (SwissArmyTileSet)target;
                    if (values.length == 2) {
                        starget.setOffsetPos(new Point(values[0], values[1]));
                    } else {
                        Log.warning("Invalid 'offsetPos' definition '" +
                                    bodyText + "'.");
                    }
                }
            });

        digester.addRule(
            _prefix + TILESET_PATH + "/gapSize",
            new CallMethodSpecialRule(digester) {
                public void parseAndSet (String bodyText, Object target)
                {
                    int[] values = StringUtil.parseIntArray(bodyText);
                    SwissArmyTileSet starget = (SwissArmyTileSet)target;
                    if (values.length == 2) {
                        starget.setGapSize(new Dimension(values[0], values[1]));
                    } else {
                        Log.warning("Invalid 'gapSize' definition '" +
                                    bodyText + "'.");
                    }
                }
            });
    }

    // documentation inherited
    public boolean isValid (Object target)
    {
        SwissArmyTileSet set = (SwissArmyTileSet)target;
        boolean valid = super.isValid(target);

        // check for a <widths> element
        if (set.getWidths() == null) {
            Log.warning("Tile set definition missing valid <widths> " +
                        "element [set=" + set + "].");
            valid = false;
        }

        // check for a <heights> element
        if (set.getHeights() == null) {
            Log.warning("Tile set definition missing valid <heights> " +
                        "element [set=" + set + "].");
            valid = false;
        }

        // check for a <tileCounts> element
        if (set.getTileCounts() == null) {
            Log.warning("Tile set definition missing valid <tileCounts> " +
                        "element [set=" + set + "].");
            valid = false;
        }

        return valid;
    }

    // documentation inherited
    protected Class getTileSetClass ()
    {
        return SwissArmyTileSet.class;
    }
}
