//
// $Id: ViewerContext.java,v 1.3 2001/08/15 00:10:59 mdb Exp $

package com.threerings.miso.viewer.util;

import com.samskivert.util.Context;

import com.threerings.miso.scene.xml.XMLFileSceneRepository;
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
    public XMLFileSceneRepository getSceneRepository ();
}
