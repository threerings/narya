//
// $Id: SimpleMisoSceneParser.java,v 1.1 2003/04/17 19:21:16 mdb Exp $

package com.threerings.miso.tools.xml;

import java.io.IOException;
import java.io.FileInputStream;

import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;

import com.samskivert.util.StringUtil;
import com.threerings.tools.xml.NestableRuleSet;

import com.threerings.miso.data.SimpleMisoSceneModel;

/**
 * A simple class for parsing simple miso scene models.
 */
public class SimpleMisoSceneParser
{
    /**
     * Constructs a scene parser that parses scenes with the specified XML
     * path prefix.
     */
    public SimpleMisoSceneParser (String prefix)
    {
        // create and configure our digester
        _digester = new Digester();

        // create our scene rule set
        SimpleMisoSceneRuleSet set = new SimpleMisoSceneRuleSet();

        // configure our top-level path prefix
        if (StringUtil.blank(prefix)) {
            _prefix = set.getOuterElement();
        } else {
            _prefix = prefix + "/" + set.getOuterElement();
        }

        // add the scene rules
        set.addRuleInstances(_prefix, _digester);

        // add a rule to grab the finished scene model
        _digester.addSetNext(
            _prefix, "setScene", SimpleMisoSceneModel.class.getName());
    }

    /**
     * Parses the XML file at the specified path into a scene model
     * instance.
     */
    public SimpleMisoSceneModel parseScene (String path)
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
    public void setScene (SimpleMisoSceneModel model)
    {
        _model = model;
    }

    protected String _prefix;
    protected Digester _digester;
    protected SimpleMisoSceneModel _model;
}
