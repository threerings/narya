//
// $Id: UniformTileSetRuleSet.java,v 1.2 2001/11/18 04:27:56 mdb Exp $

package com.threerings.media.tile.xml;

import org.xml.sax.Attributes;
import org.apache.commons.digester.Digester;

import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.UniformTileSet;

/**
 * Parses {@link UniformTileSet} instances from a tileset description. A
 * uniform tileset description looks like so:
 *
 * <pre>
 * &lt;tileset name="Sample Uniform Tileset"&gt;
 *   &lt;imgpath&gt;path/to/image.png&lt;/imgpath&gt;
 *   &lt;!-- the width of each tile in pixels --&gt;
 *   &lt;width&gt;64&lt;/width&gt;
 *   &lt;!-- the height of each tile in pixels --&gt;
 *   &lt;height&gt;48&lt;/height&gt;
 *   &lt;!-- the total number of tiles in the set --&gt;
 *   &lt;tileCount&gt;16&lt;/tileCount&gt;
 * &lt;/tileset&gt;
 * </pre>
 */
public class UniformTileSetRuleSet extends TileSetRuleSet
{
    /**
     * Constructs a uniform tileset rule set that will match tilesets with
     * the specified prefix. See the documentation for {@link
     * TileSetRuleSet#TileSetRuleSet} for more info on matching.
     */
    public UniformTileSetRuleSet (String prefix)
    {
        super(prefix);
    }

    // documentation inherited
    public void addRuleInstances (Digester digester)
    {
        super.addRuleInstances(digester);

        digester.addCallMethod(
            _prefix + "/tileset/width", "setWidth", 0,
            new Class[] { java.lang.Integer.TYPE });
        digester.addCallMethod(
            _prefix + "/tileset/height", "setHeight", 0,
            new Class[] { java.lang.Integer.TYPE });
        digester.addCallMethod(
            _prefix + "/tileset/tileCount", "setTileCount", 0,
            new Class[] { java.lang.Integer.TYPE });
    }

    // documentation inherited
    protected TileSet createTileSet (Attributes attributes)
    {
        // we use uniform tilesets
        return new UniformTileSet();
    }
}
