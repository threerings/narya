//
// $Id: MisoSceneParser.java,v 1.1 2001/11/29 20:34:13 mdb Exp $

package com.threerings.miso.tools.scene.xml;

import java.io.IOException;
import java.io.FileInputStream;

import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;

import com.threerings.miso.scene.MisoSceneModel;

/**
 * A simple class for parsing a standalone miso scene model.
 */
public class MisoSceneParser
{
    /**
     * Constructs a miso scene parser that parses scenes with the
     * specified XML path prefix. See the {@link
     * MisoSceneRuleSet#MisoSceneRuleSet} documentation for more
     * information.
     */
    public MisoSceneParser (String prefix)
    {
        // create and configure our digester
        _digester = new Digester();
        MisoSceneRuleSet set = new MisoSceneRuleSet();
        set.setPrefix(prefix);
        _digester.addRuleSet(set);
        _digester.addSetNext(prefix, "setSceneModel",
                             MisoSceneModel.class.getName());
    }

    /**
     * Parses the XML file at the specified path into a miso scene model.
     */
    public MisoSceneModel parseScene (String path)
        throws IOException, SAXException
    {
        _model = null;
        _digester.push(this);
        _digester.parse(new FileInputStream(path));
        return _model;
    }

    /**
     * Called by the parser once the scene is parsed.
     */
    public void setSceneModel (MisoSceneModel model)
    {
        _model = model;
    }

    protected Digester _digester;
    protected MisoSceneModel _model;
}
