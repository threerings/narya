//
// $Id: SpotSceneParserTest.java,v 1.5 2003/04/17 19:21:17 mdb Exp $

package com.threerings.whirled.spot.tools.xml;

import com.samskivert.test.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.tools.xml.SceneParser;

public class SpotSceneParserTest extends TestCase
{
    public SpotSceneParserTest ()
    {
        super(SpotSceneParserTest.class.getName());
    }

    public void runTest ()
    {
        try {
            SceneParser parser = new SceneParser("scene");
            parser.registerAuxRuleSet(new SpotSceneRuleSet());
            String tspath = TestUtil.getResourcePath(TEST_SCENE_PATH);
            SceneModel scene = parser.parseScene(tspath);
            System.out.println("Parsed " + scene + ".");

        } catch (Exception e) {
            e.printStackTrace();
            fail("Test threw exception");
        }
    }

    public static Test suite ()
    {
        return new SpotSceneParserTest();
    }

    public static void main (String[] args)
    {
        SpotSceneParserTest test = new SpotSceneParserTest();
        test.runTest();
    }

    protected static final String TEST_SCENE_PATH =
        "rsrc/whirled/spot/tools/xml/scene.xml";
}
