//
// $Id: IsoSceneViewModelListener.java,v 1.1 2001/10/18 21:01:44 shaper Exp $

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
    /**
     * Called by the {@link IsoSceneView} when the model is changed.
     */
    public void viewChanged (int event);

    /** Notification event constants. */
    public static final int SHOW_LOCATIONS_CHANGED = 0;
    public static final int SHOW_COORDINATES_CHANGED = 1;
}
