//
// $Id: RuntimeMisoScene.java,v 1.2 2003/01/31 23:10:45 mdb Exp $

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
}
