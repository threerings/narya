//
// $Id: TileSetRuleSet.java,v 1.4 2001/11/21 02:42:15 mdb Exp $

package com.threerings.media.tools.tile.xml;

import org.xml.sax.Attributes;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSetBase;

import com.threerings.media.tile.TileSet;

/**
 * The tileset rule set is used to parse the base attributes of a tileset
 * instance. Derived classes would extend this and add rules for their own
 * special tilesets.
 */
public abstract class TileSetRuleSet extends RuleSetBase
{
    /** The component of the digester path that is appended by the tileset
     * rule set to match a tileset. This is appended to whatever prefix is
     * provided to the tileset rule set to obtain the complete XML path to
     * a matched tileset. */
    public static final String TILESET_PATH = "/tileset";

    /**
     * Instructs the tileset rule set to match tilesets with the supplied
     * prefix. For example, passing a prefix of
     * <code>tilesets.objectsets</code> will match tilesets in the
     * following XML file:
     *
     * <pre>
     * &lt;tilesets&gt;
     *   &lt;objectsets&gt;
     *     &lt;tileset&gt;
     *       // ...
     *     &lt;/tileset&gt;
     *   &lt;/objectsets&gt;
     * &lt;/tilesets&gt;
     * </pre>
     *
     * This must be called before adding the ruleset to a digester.
     */
    public void setPrefix (String prefix)
    {
        _prefix = prefix;
    }

    /**
     * Adds the necessary rules to the digester to parse our tilesets.
     * Derived classes should override this method, being sure to call the
     * superclass method and then adding their own rule instances (which
     * should register themselves relative to the <code>_prefix</code>
     * member).
     */
    public void addRuleInstances (Digester digester)
    {
        // this creates the appropriate instance when we encounter a
        // <tileset> tag
        digester.addObjectCreate(_prefix + TILESET_PATH,
                                 getTileSetClass().getName());

        // grab the name attribute from the <tileset> tag
        digester.addSetProperties(_prefix + TILESET_PATH);

        // grab the image path from an element
        digester.addCallMethod(
            _prefix + TILESET_PATH + "/imagePath", "setImagePath", 0);
    }

    /**
     * A tileset rule set will create tilesets of a particular class,
     * which must be provided by the derived class via this method.
     */
    protected abstract Class getTileSetClass ();

    /** The prefix at which me match our tilesets. */
    protected String _prefix;
}
