//
// $Id: RuntimeMisoScene.java,v 1.1 2002/12/11 23:07:21 shaper Exp $

package com.threerings.miso.server;

import java.util.Iterator;

import com.threerings.miso.scene.SceneObject;

/**
 * The Miso scene interface used by the server to deal with scenes.
 */
public interface RuntimeMisoScene
{
    /**
     * Iterates over the {@link RuntimeSceneObject} instances representing
     * all available objects in the scene.
     */
    public Iterator enumerateSceneObjects ();
}
