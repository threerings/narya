//
// $Id: SpotSceneParserTest.java,v 1.1 2001/12/05 03:38:09 mdb Exp $

package com.threerings.whirled.tools.spot.xml;

import com.samskivert.test.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;

import com.threerings.whirled.tools.spot.EditableSpotScene;

public class SpotSceneParserTest extends TestCase
{
    public SpotSceneParserTest ()
    {
        super(SpotSceneParserTest.class.getName());
    }

    public void runTest ()
    {
        try {
            SpotSceneParser parser = new SpotSceneParser("scene");
            String tspath = TestUtil.getResourcePath(TEST_SCENE_PATH);
            EditableSpotScene scene = parser.parseScene(tspath);
            System.out.println("Parsed " + scene.getSpotSceneModel() + ".");

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
        "whirled/tools/spot/xml/scene.xml";
}
