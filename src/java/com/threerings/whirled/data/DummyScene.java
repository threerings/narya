//
// $Id: DummyScene.java,v 1.3 2001/09/21 00:21:40 mdb Exp $

package com.threerings.whirled.client.test;

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

    protected int _sceneId;
}
