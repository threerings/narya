//
// $Id: DummyScene.java,v 1.5 2001/10/11 04:07:54 mdb Exp $

package com.threerings.whirled.test;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.data.Scene;

public class DummyScene implements Scene
{
    public DummyScene (int sceneId)
    {
        _sceneId = sceneId;
    }

    public int getId ()
    {
        return _sceneId;
    }

    public int getVersion ()
    {
        return 1;
    }

    public String getName ()
    {
        return "scene";
    }

    public int[] getNeighborIds ()
    {
        return null;
    }

    public PlaceConfig getPlaceConfig ()
    {
        return new TestConfig();
    }

    protected int _sceneId;
}
