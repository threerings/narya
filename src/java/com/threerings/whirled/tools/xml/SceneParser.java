//
// $Id: SceneParser.java,v 1.1 2001/12/05 03:38:09 mdb Exp $

package com.threerings.whirled.tools.xml;

import java.io.IOException;
import java.io.FileInputStream;

import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;

import com.threerings.whirled.tools.EditableScene;
import com.threerings.whirled.tools.EditableSceneImpl;

/**
 * A simple class for parsing an editable scene instance.
 */
public class SceneParser
{
    /**
     * Constructs a scene parser that parses scenes with the specified XML
     * path prefix. See the {@link SceneRuleSet#SceneRuleSet}
     * documentation for more information.
     */
    public SceneParser (String prefix)
    {
        // create and configure our digester
        _digester = new Digester();
        SceneRuleSet set = new SceneRuleSet();
        set.setPrefix(prefix);
        _digester.addRuleSet(set);
        _digester.addSetNext(prefix, "setScene", EditableScene.class.getName());
    }

    /**
     * Parses the XML file at the specified path into an editable scene
     * instance.
     */
    public EditableScene parseScene (String path)
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
    public void setScene (EditableScene scene)
    {
        _scene = scene;
    }

    protected Digester _digester;
    protected EditableScene _scene;
}
