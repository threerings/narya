//
// $Id: TileSetRuleSet.java,v 1.1 2001/11/18 04:09:22 mdb Exp $

package com.threerings.media.tile.xml;

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
    /**
     * Constructs a tileset rule set which will match tilesets with the
     * supplied prefix. For example, passing a prefix of
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
     */
    public TileSetRuleSet (String prefix)
    {
        _prefix = prefix;
    }

    /**
     * Called by the {@link XMLTileSetParser} to initialize this tileset
     * rule set with the necessary back references to operate properly.
     */
    protected void init (XMLTileSetParser parser)
    {
        _parser = parser;
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
        digester.addRule(_prefix + "/tileset",
                         new TileSetCreateRule(digester));

        // grab the name attribute from the <tileset> tag
        digester.addSetProperties(_prefix + "/tileset");

        // grab the image path from an element
        digester.addCallMethod(
            _prefix + "/tileset/imagePath", "setImagePath", 0);
    }

    /**
     * When a &lt;tileset&gt; element is encountered, this method is
     * called to create a new instance of {@link TileSet}. Though the
     * attributes are supplied (in case an attribute is needed to
     * determine which derived instance of {@link TileSet} to create, this
     * method should not configure the created tileset object. It should
     * instead rely on the set properties rule that will be executed after
     * this object is created or to custom set property rules registered
     * in {@link #addDigesterRules}.
     */
    protected abstract TileSet createTileSet (Attributes attributes);

    /**
     * Used to process a &lt;tileset&gt; element.
     */
    protected class TileSetCreateRule extends Rule
    {
        public TileSetCreateRule (Digester digester)
        {
            super(digester);
        }

        public void begin (Attributes attributes)
            throws Exception
        {
            // pass the torch to the XML parser to create the tileset
            TileSet set = createTileSet(attributes);
            // then push it onto the stack
            digester.push(set);
        }

        public void end ()
            throws Exception
        {
            // pop the tileset off of the stack
            TileSet set = (TileSet)digester.pop();
            // and stick it into our tileset map
            _parser._tilesets.put(set.getName(), set);
        }
    }

    /** The prefix at which me match our tilesets. */
    protected String _prefix;

    /** A reference to the XMLTileSetParser on whose behalf we are
     * parsing. */
    protected XMLTileSetParser _parser;
}
