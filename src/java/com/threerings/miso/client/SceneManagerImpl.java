//
// $Id: SceneManagerImpl.java,v 1.6 2001/07/23 22:31:47 shaper Exp $

package com.threerings.miso.scene;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.samskivert.util.*;
import com.threerings.miso.Log;
import com.threerings.miso.tile.TileManager;

public abstract class SceneManagerImpl implements SceneManager
{
    public void init (TileManager tilemgr)
    {
        _tilemgr = tilemgr;
    }

    public Scene getScene (int sid)
    {
	// TBD
	return null;
    }

    public String[] getLayerNames ()
    {
	return Scene.XLATE_LAYERS;
    }

    public ArrayList getAllScenes ()
    {
	ArrayList list = new ArrayList();
	CollectionUtil.addAll(list, _scenes.elements());
	return list;
    }

    public Scene getNewScene ()
    {
        Scene scene = new Scene(_tilemgr, _sid);
        _scenes.put(_sid++, scene);
        return scene;
    }

    /** The next monotonically-increasing scene id. */
    protected int _sid = 0;

    /** The tile manager for use by all scenes. */
    protected TileManager _tilemgr;

    /** The scenes currently available. */
    protected IntMap _scenes = new IntMap();
}
