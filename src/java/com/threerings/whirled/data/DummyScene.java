//
// $Id: DummyScene.java,v 1.6 2001/11/08 02:07:36 mdb Exp $

package com.threerings.whirled;

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
