//
// $Id: ViewerContext.java,v 1.2 2001/08/15 00:00:52 mdb Exp $

package com.threerings.miso.viewer.util;

import com.samskivert.util.Context;

import com.threerings.miso.scene.SceneRepository;
import com.threerings.miso.util.MisoContext;

/**
 * A mix-in interface that combines the MisoContext and Context
 * interfaces to provide an interface with the best of both worlds.
 */
public interface ViewerContext extends MisoContext, Context
{
    /**
     * Returns the scene repository that we can use to get scenes.
     */
    public SceneRepository getSceneRepository ();
}
