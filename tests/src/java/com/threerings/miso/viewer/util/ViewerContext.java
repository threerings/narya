//
// $Id: ViewerContext.java,v 1.5 2001/11/02 02:52:16 shaper Exp $

package com.threerings.miso.viewer.util;

import com.samskivert.util.Context;

import com.threerings.miso.scene.xml.XMLSceneRepository;
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
    public XMLSceneRepository getSceneRepository ();

    /**
     * Returns the viewer data model.
     */
    public ViewerModel getViewerModel ();
}
