//
// $Id: DummyScene.java,v 1.7 2001/11/08 02:59:17 mdb Exp $

package com.threerings.whirled.data;

import com.threerings.crowd.data.PlaceConfig;

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
        return null;
    }

    protected int _sceneId;
}
