//
// $Id: SwissArmyTileSetRuleSet.java,v 1.1 2001/11/18 04:09:22 mdb Exp $

package com.threerings.media.tile.xml;

import java.awt.Point;

import org.xml.sax.Attributes;
import org.apache.commons.digester.Digester;

import com.samskivert.util.StringUtil;
import com.samskivert.xml.CallMethodSpecialRule;

import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.SwissArmyTileSet;

/**
 * Parses {@link SwissArmyTileSet} instances from a tileset description. A
 * uniform tileset description looks like so:
 *
 * <pre>
 * &lt;tileset name="Sample Swiss Army Tileset"&gt;
 *   &lt;imgpath&gt;path/to/image.png&lt;/imgpath&gt;
 *   &lt;!-- the widths (per row) of each tile in pixels --&gt;
 *   &lt;widths&gt;64, 64, 64, 64&lt;/widths&gt;
 *   &lt;!-- the heights (per row) of each tile in pixels --&gt;
 *   &lt;heights&gt;48, 48, 48, 64&lt;/heights&gt;
 *   &lt;!-- the number of tiles in each row --&gt;
 *   &lt;tileCounts&gt;16, 5, 3, 10&lt;/tileCounts&gt;
 *   &lt;!-- the offset in pixels to the upper left tile --&gt;
 *   &lt;offset&gt;8, 8&lt;/offset&gt;
 *   &lt;!-- the gap between tiles in pixels --&gt;
 *   &lt;gap&gt;12, 12&lt;/gap&gt;
 * &lt;/tileset&gt;
 * </pre>
 */
public class SwissArmyTileSetRuleSet extends TileSetRuleSet
{
    /**
     * Constructs a uniform tileset rule set that will match tilesets with
     * the specified prefix. See the documentation for {@link
     * TileSetRuleSet#TileSetruleSet} for more info on matching.
     */
    public SwissArmyTileSetRuleSet (String prefix)
    {
        super(prefix);
    }

    // documentation inherited
    public void addRuleInstances (Digester digester)
    {
        super.addRuleInstances(digester);

        digester.addRule(
            _prefix + "/tileset/widths",
            new CallMethodSpecialRule(digester) {
                public void parseAndSet (String bodyText, Object target)
                {
                    int[] widths = StringUtil.parseIntArray(bodyText);
                    ((SwissArmyTileSet)target).setWidths(widths);
                }
            });

        digester.addRule(
            _prefix + "/tileset/heights",
            new CallMethodSpecialRule(digester) {
                public void parseAndSet (String bodyText, Object target)
                {
                    int[] heights = StringUtil.parseIntArray(bodyText);
                    ((SwissArmyTileSet)target).setHeights(heights);
                }
            });

        digester.addRule(
            _prefix + "/tileset/tileCounts",
            new CallMethodSpecialRule(digester) {
                public void parseAndSet (String bodyText, Object target)
                {
                    int[] tileCounts = StringUtil.parseIntArray(bodyText);
                    ((SwissArmyTileSet)target).setTileCounts(tileCounts);
                }
            });
    }

    // documentation inherited
    protected TileSet createTileSet (Attributes attributes)
    {
        // we use uniform tilesets
        return new SwissArmyTileSet();
    }

    /**
     * Converts a string containing values as (x, y) into the
     * corresponding integer values and populates the given point
     * object.
     *
     * @param str the point values in string format.
     * @param point the point object to populate.
     */
    protected void parsePoint (String str, Point point)
    {
        int vals[] = StringUtil.parseIntArray(str);
        point.setLocation(vals[0], vals[1]);
    }
}
