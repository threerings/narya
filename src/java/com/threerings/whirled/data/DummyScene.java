//
// $Id: DummyScene.java,v 1.2 2001/08/16 18:05:17 shaper Exp $

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

    public int[] getPortalIds ()
    {
        return null;
    }

    protected int _sceneId;
}
