//
// $Id: CrowdContext.java,v 1.8 2002/11/08 09:31:59 mdb Exp $

package com.threerings.crowd.util;

import com.threerings.presents.util.PresentsContext;
import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.client.OccupantDirector;
import com.threerings.crowd.client.PlaceView;

/**
 * The crowd context provides access to the various managers, etc. that
 * are needed by the crowd client code.
 */
public interface CrowdContext extends PresentsContext
{
    /**
     * Returns a reference to the location director.
     */
    public LocationDirector getLocationDirector ();

    /**
     * Returns a reference to the occupant director.
     */
    public OccupantDirector getOccupantDirector ();

    /**
     * When the client enters a new place, the location director creates a
     * place controller which then creates a place view to visualize the
     * place for the user. The place view created by the place controller
     * will be passed to this function to actually display it in whatever
     * user interface is provided for the user. We don't require any
     * particular user interface toolkit, so it is expected that the place
     * view implementation will coordinate with the client implementation
     * so that the client can display the view provided by the place
     * controller.
     *
     * <p> Though the place view is created before we enter the place, it
     * won't be displayed (via a call to this function) until we have
     * fully entered the place and are ready for user interaction.
     */
    public void setPlaceView (PlaceView view);

    /**
     * When the client leaves a place, the place controller will remove
     * any place view it set previously via {@link #setPlaceView} with a
     * call to this method.
     */
    public void clearPlaceView (PlaceView view);
}
