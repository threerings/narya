//
// $Id: IsoSceneViewModelListener.java,v 1.3 2001/11/18 04:09:22 mdb Exp $

package com.threerings.miso.scene;

/**
 * The iso scene view model listener interface should be implemented
 * by classes that would like to be notified when the iso scene view
 * model is changed.
 *
 * @see IsoSceneViewModel
 */
public interface IsoSceneViewModelListener
{
    /** Notification event constant indicating that the "show coordinates"
     * configuration has changed.. */
    public static final int SHOW_COORDINATES_CHANGED = 0;

    /**
     * Called by the {@link com.threerings.miso.scene.IsoSceneView} when
     * the model is changed.
     */
    public void viewChanged (int event);
}
