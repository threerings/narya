//
// $Id: SpotSceneParser.java,v 1.1 2001/12/05 03:38:09 mdb Exp $

package com.threerings.whirled.tools.spot.xml;

import java.io.IOException;
import java.io.FileInputStream;

import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;

import com.threerings.whirled.tools.spot.EditableSpotScene;
import com.threerings.whirled.tools.spot.EditableSpotSceneImpl;

/**
 * A simple class for parsing an editable spot scene instance.
 */
public class SpotSceneParser
{
    /**
     * Constructs a scene parser that parses scenes with the specified XML
     * path prefix. See the {@link SpotSceneRuleSet#SpotSceneRuleSet}
     * documentation for more information.
     */
    public SpotSceneParser (String prefix)
    {
        // create and configure our digester
        _digester = new Digester();
        SpotSceneRuleSet set = new SpotSceneRuleSet();
        set.setPrefix(prefix);
        _digester.addRuleSet(set);
        _digester.addSetNext(prefix, "setScene",
                             EditableSpotScene.class.getName());
    }

    /**
     * Parses the XML file at the specified path into an editable scene
     * instance.
     */
    public EditableSpotScene parseScene (String path)
        throws IOException, SAXException
    {
        _scene = null;
        _digester.push(this);
        _digester.parse(new FileInputStream(path));
        return _scene;
    }

    /**
     * Called by the parser once the scene is parsed.
     */
    public void setScene (EditableSpotScene scene)
    {
        _scene = scene;
    }

    protected Digester _digester;
    protected EditableSpotScene _scene;
}
