//
// $Id: MisoSceneParser.java,v 1.3 2003/01/31 23:10:46 mdb Exp $

package com.threerings.miso.tools.xml;

import java.io.IOException;
import java.io.FileInputStream;

import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;

import com.threerings.miso.data.MisoSceneModel;

/**
 * A simple class for parsing a standalone miso scene model.
 */
public class MisoSceneParser
{
    /**
     * Constructs a miso scene parser that parses scenes with the
     * specified XML path prefix and a standard scene rule set. See the
     * {@link MisoSceneRuleSet#MisoSceneRuleSet} documentation for more
     * information.
     */
    public MisoSceneParser (String prefix)
    {
        MisoSceneRuleSet set = new MisoSceneRuleSet();
        set.setPrefix(prefix);
        init(prefix, set);
    }

    /**
     * Constructs a miso scene parser that parses scenes with the
     * specified XML path prefix, using the supplied rule set. The rule
     * set should leave a {@link MisoSceneModel} at the top of the
     * digester's stack.
     */
    public MisoSceneParser (String prefix, RuleSet rules)
    {
        init(prefix, rules);
    }

    /** Constructor helper function. */
    protected void init (String prefix, RuleSet rules)
    {
        // create and configure our digester
        _digester = new Digester();
        _digester.addRuleSet(rules);
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
