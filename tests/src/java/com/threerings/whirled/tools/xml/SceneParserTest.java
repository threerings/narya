//
// $Id: SceneParserTest.java,v 1.3 2002/02/09 07:50:04 mdb Exp $

package com.threerings.whirled.tools.xml;

import com.samskivert.test.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;

import com.threerings.whirled.tools.EditableScene;

public class SceneParserTest extends TestCase
{
    public SceneParserTest ()
    {
        super(SceneParserTest.class.getName());
    }

    public void runTest ()
    {
        try {
            SceneParser parser = new SceneParser("scene");
            String tspath = TestUtil.getResourcePath(TEST_SCENE_PATH);
            EditableScene scene = parser.parseScene(tspath);
            // System.out.println("Parsed " + scene.getSceneModel() + ".");

        } catch (Exception e) {
            e.printStackTrace();
            fail("Test threw exception");
        }
    }

    public static Test suite ()
    {
        return new SceneParserTest();
    }

    public static void main (String[] args)
    {
        SceneParserTest test = new SceneParserTest();
        test.runTest();
    }

    protected static final String TEST_SCENE_PATH =
        "rsrc/whirled/tools/xml/scene.xml";
}
