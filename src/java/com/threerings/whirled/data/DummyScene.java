//
// $Id: DummyScene.java,v 1.1 2001/08/14 06:51:07 mdb Exp $

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

    public int[] getExitIds ()
    {
        return null;
    }

    protected int _sceneId;
}
