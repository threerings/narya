//
// $Id: ViewerContext.java,v 1.4 2001/09/05 00:45:27 shaper Exp $

package com.threerings.miso.viewer.util;

import com.samskivert.util.Context;

import com.threerings.miso.scene.xml.XMLFileSceneRepository;
import com.threerings.miso.util.MisoContext;
import com.threerings.miso.viewer.ViewerModel;

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

    /**
     * Returns the viewer data model.
     */
    public ViewerModel getViewerModel ();
}
