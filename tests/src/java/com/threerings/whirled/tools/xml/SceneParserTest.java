//
// $Id: SceneParserTest.java,v 1.5 2003/04/17 19:21:17 mdb Exp $

package com.threerings.whirled.tools.xml;

import com.samskivert.test.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;

import com.threerings.whirled.data.SceneModel;

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
            SceneModel scene = parser.parseScene(tspath);
            System.out.println("Parsed " + scene + ".");

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
