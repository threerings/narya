//
// $Id: RuntimeMisoScene.java,v 1.3 2003/02/04 17:24:56 mdb Exp $

package com.threerings.miso.server;

import java.util.Iterator;

import com.threerings.miso.data.ObjectInfo;

/**
 * The Miso scene interface used by the server to deal with scenes.
 */
public interface RuntimeMisoScene
{
    /**
     * Iterates over the {@link ObjectInfo} instances representing all
     * "interesting" objects in the scene.
     */
    public Iterator enumerateObjects ();

    /**
     * Adds the supplied object to this scene and immediately updates the
     * underlying scene model.
     */
    public void addObject (ObjectInfo info);
}
