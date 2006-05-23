//
// $Id: StageSceneParser.java 16546 2004-07-27 20:49:56Z ray $

package com.threerings.stage.tools.xml;

import org.apache.commons.digester.Rule;

import org.xml.sax.Attributes;

import com.threerings.whirled.spot.tools.xml.SpotSceneRuleSet;
import com.threerings.whirled.tools.xml.SceneParser;
import com.threerings.whirled.tools.xml.SceneRuleSet;

import com.threerings.whirled.spot.data.Location;

import com.threerings.stage.data.StageLocation;
import com.threerings.stage.data.StageSceneModel;

/**
 * Parses {@link StageSceneModel} instances from an XML description file.
 */
public class StageSceneParser extends SceneParser
{
    /**
     * Constructs a parser that can be used to parse Stage scene models.
     */
    public StageSceneParser ()
    {
        super("");

        // add a rule to parse scene colorizations
        _digester.addRule("scene/zations/zation", new Rule() {
            public void begin (String namespace, String name,
                               Attributes attrs) throws Exception {
                StageSceneModel yoscene = (StageSceneModel) digester.peek();
                int classId = Integer.parseInt(attrs.getValue("classId"));
                int colorId = Integer.parseInt(attrs.getValue("colorId"));
                yoscene.setDefaultColor(classId, colorId);
            }
        });

        // add rule sets for our aux scene models
        registerAuxRuleSet(new SpotSceneRuleSet() {
            protected Location createLocation () {
                return new StageLocation();
            }
        });
        registerAuxRuleSet(new StageMisoSceneRuleSet());
    }

    // documentation inherited from interface
    protected SceneRuleSet createSceneRuleSet ()
    {
        return new StageSceneRuleSet();
    }

    /**
     * A simple hook for parsing a single scene from the command line.
     */
    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: StageSceneParser scene.xml");
            System.exit(-1);
        }

        try {
            System.out.println(
                "Parsed " + new StageSceneParser().parseScene(args[0]));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
