//
// $Id: ObjectTileSetRuleSet.java,v 1.11 2004/07/13 16:34:49 mdb Exp $

package com.threerings.media.tile.tools.xml;

import org.apache.commons.digester.Digester;

import com.samskivert.util.StringUtil;
import com.samskivert.xml.CallMethodSpecialRule;

import com.threerings.media.tile.ObjectTileSet;

import com.threerings.util.DirectionUtil;

/**
 * Parses {@link ObjectTileSet} instances from a tileset description. An
 * object tileset description looks like so:
 *
 * <pre>
 * &lt;tileset name="Sample Object Tileset"&gt;
 *   &lt;imagePath&gt;path/to/image.png&lt;/imagePath&gt;
 *   &lt;!-- the widths (per row) of each tile in pixels --&gt;
 *   &lt;widths&gt;265&lt;/widths&gt;
 *   &lt;!-- the heights (per row) of each tile in pixels --&gt;
 *   &lt;heights&gt;224&lt;/heights&gt;
 *   &lt;!-- the number of tiles in each row --&gt;
 *   &lt;tileCounts&gt;4&lt;/tileCounts&gt;
 *   &lt;!-- the offset in pixels to the upper left tile --&gt;
 *   &lt;offsetPos&gt;0, 0&lt;/offsetPos&gt;
 *   &lt;!-- the gap between tiles in pixels --&gt;
 *   &lt;gapSize&gt;0, 0&lt;/gapSize&gt;
 *   &lt;!-- the widths (in unit tile count) of the objects --&gt;
 *   &lt;objectWidths&gt;4, 3, 4, 3&lt;/objectWidths&gt;
 *   &lt;!-- the heights (in unit tile count) of the objects --&gt;
 *   &lt;objectHeights&gt;3, 4, 3, 4&lt;/objectHeights&gt;
 *   &lt;!-- the default render priorities for these object tiles --&gt;
 *   &lt;priorities&gt;0, 0, -1, 0&lt;/priorities&gt;
 * &lt;/tileset&gt;
 * </pre>
 */
public class ObjectTileSetRuleSet extends SwissArmyTileSetRuleSet
{
    // documentation inherited
    public void addRuleInstances (Digester digester)
    {
        super.addRuleInstances(digester);

        digester.addRule(
            _prefix + TILESET_PATH + "/objectWidths",
            new CallMethodSpecialRule() {
                public void parseAndSet (String bodyText, Object target)
                {
                    int[] widths = StringUtil.parseIntArray(bodyText);
                    ((ObjectTileSet)target).setObjectWidths(widths);
                }
            });

        digester.addRule(
            _prefix + TILESET_PATH + "/objectHeights",
            new CallMethodSpecialRule() {
                public void parseAndSet (String bodyText, Object target)
                {
                    int[] heights = StringUtil.parseIntArray(bodyText);
                    ((ObjectTileSet)target).setObjectHeights(heights);
                }
            });

        digester.addRule(
            _prefix + TILESET_PATH + "/xOrigins", new CallMethodSpecialRule() {
                public void parseAndSet (String bodyText, Object target)
                {
                    int[] xorigins = StringUtil.parseIntArray(bodyText);
                    ((ObjectTileSet)target).setXOrigins(xorigins);
                }
            });

        digester.addRule(
            _prefix + TILESET_PATH + "/yOrigins", new CallMethodSpecialRule() {
                public void parseAndSet (String bodyText, Object target)
                {
                    int[] yorigins = StringUtil.parseIntArray(bodyText);
                    ((ObjectTileSet)target).setYOrigins(yorigins);
                }
            });

        digester.addRule(
            _prefix + TILESET_PATH + "/priorities",
            new CallMethodSpecialRule() {
                public void parseAndSet (String bodyText, Object target)
                {
                    byte[] prios = StringUtil.parseByteArray(bodyText);
                    ((ObjectTileSet)target).setPriorities(prios);
                }
            });

        digester.addRule(
            _prefix + TILESET_PATH + "/zations", new CallMethodSpecialRule() {
                public void parseAndSet (String bodyText, Object target)
                {
                    String[] zations = StringUtil.parseStringArray(bodyText);
                    ((ObjectTileSet)target).setColorizations(zations);
                }
            });

        digester.addRule(
            _prefix + TILESET_PATH + "/xspots", new CallMethodSpecialRule() {
                public void parseAndSet (String bodyText, Object target)
                {
                    short[] xspots = StringUtil.parseShortArray(bodyText);
                    ((ObjectTileSet)target).setXSpots(xspots);
                }
            });

        digester.addRule(
            _prefix + TILESET_PATH + "/yspots", new CallMethodSpecialRule() {
                public void parseAndSet (String bodyText, Object target)
                {
                    short[] yspots = StringUtil.parseShortArray(bodyText);
                    ((ObjectTileSet)target).setYSpots(yspots);
                }
            });

        digester.addRule(
            _prefix + TILESET_PATH + "/sorients", new CallMethodSpecialRule() {
                public void parseAndSet (String bodyText, Object target)
                {
                    ObjectTileSet set = (ObjectTileSet)target;
                    String[] ostrs = StringUtil.parseStringArray(bodyText);
                    byte[] sorients = new byte[ostrs.length];
                    for (int ii = 0; ii < sorients.length; ii++) {
                        sorients[ii] = (byte)
                            DirectionUtil.fromShortString(ostrs[ii]);
                        if ((sorients[ii] == DirectionUtil.NONE) &&
                            // don't complain if they didn't even try to
                            // specify a valid direction
                            (! ostrs[ii].equals("-1"))) {
                            System.err.println("Invalid spot orientation " +
                                               "[set=" + set.getName() +
                                               ", idx=" + ii +
                                               ", orient=" + ostrs[ii] + "].");
                        }
                    }
                    set.setSpotOrients(sorients);
                }
            });
    }

    // documentation inherited
    protected Class getTileSetClass ()
    {
        return ObjectTileSet.class;
    }
}
