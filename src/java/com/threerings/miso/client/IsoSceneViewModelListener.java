//
// $Id: IsoSceneViewModelListener.java,v 1.4 2002/06/17 18:01:47 shaper Exp $

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

    /** Notification event constant indicating that the "show footprints"
     * configuration has changed.. */
    public static final int SHOW_FOOTPRINTS_CHANGED = 1;

    /**
     * Called by the {@link com.threerings.miso.scene.IsoSceneView} when
     * the model is changed.
     */
    public void viewChanged (int event);
}
