//
// $Id: SceneParser.java,v 1.3 2004/08/27 02:20:48 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.whirled.tools.xml;

import java.io.IOException;
import java.io.FileInputStream;

import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;

import com.samskivert.util.StringUtil;
import com.threerings.tools.xml.NestableRuleSet;
import com.threerings.whirled.data.AuxModel;
import com.threerings.whirled.data.SceneModel;

/**
 * A simple class for parsing an editable scene instance.
 */
public class SceneParser
{
    /**
     * Constructs a scene parser that parses scenes with the specified XML
     * path prefix.
     */
    public SceneParser (String prefix)
    {
        // create and configure our digester
        _digester = new Digester();

        // create our scene rule set
        SceneRuleSet set = createSceneRuleSet();

        // configure our top-level path prefix
        if (StringUtil.blank(prefix)) {
            _prefix = set.getOuterElement();
        } else {
            _prefix = prefix + "/" + set.getOuterElement();
        }

        // add the scene rules
        set.addRuleInstances(_prefix, _digester);

        // add a rule to grab the finished scene model
        _digester.addSetNext(_prefix, "setScene", SceneModel.class.getName());
    }

    /**
     * Creates the rule set used to parse our scene.
     */
    protected SceneRuleSet createSceneRuleSet ()
    {
        return new SceneRuleSet();
    }

    /**
     * Adds a {@link NestableRuleSet} for parsing auxiliary scene models.
     */
    public void registerAuxRuleSet (NestableRuleSet set)
    {
        // add their outer element to the prefix
        String prefix = _prefix + "/" + set.getOuterElement();

        // add the rules to generate the aux scene model
        set.addRuleInstances(prefix, _digester);

        // and add a rule to grab it
        _digester.addSetNext(prefix, "addAuxModel", AuxModel.class.getName());
    }

    /**
     * Parses the XML file at the specified path into a scene model
     * instance.
     */
    public SceneModel parseScene (String path)
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
    public void setScene (SceneModel model)
    {
        _model = model;
    }

    protected String _prefix;
    protected Digester _digester;
    protected SceneModel _model;
}
