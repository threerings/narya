//
// $Id: SparseMisoSceneParser.java,v 1.1 2003/04/19 22:40:34 mdb Exp $

package com.threerings.miso.tools.xml;

import java.io.IOException;
import java.io.FileInputStream;

import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;

import com.samskivert.util.StringUtil;
import com.threerings.tools.xml.NestableRuleSet;

import com.threerings.miso.data.SparseMisoSceneModel;

/**
 * A simple class for parsing simple miso scene models.
 */
public class SparseMisoSceneParser
{
    /**
     * Constructs a scene parser that parses scenes with the specified XML
     * path prefix.
     */
    public SparseMisoSceneParser (String prefix)
    {
        // create and configure our digester
        _digester = new Digester();

        // create our scene rule set
        SparseMisoSceneRuleSet set = new SparseMisoSceneRuleSet();

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
            _prefix, "setScene", SparseMisoSceneModel.class.getName());
    }

    /**
     * Parses the XML file at the specified path into a scene model
     * instance.
     */
    public SparseMisoSceneModel parseScene (String path)
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
    public void setScene (SparseMisoSceneModel model)
    {
        _model = model;
    }

    protected String _prefix;
    protected Digester _digester;
    protected SparseMisoSceneModel _model;
}
