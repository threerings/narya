//
// $Id: PlaceController.java,v 1.5 2001/10/12 19:31:15 mdb Exp $

package com.threerings.crowd.client;

import java.awt.event.ActionEvent;
import com.samskivert.swing.Controller;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

/**
 * Controls the user interface that is used to display a place. When the
 * client moves to a new place, the appropriate place controller is
 * constructed and requested to create and display the appopriate user
 * interface for that place.
 */
public abstract class PlaceController
     extends Controller
{
    /**
     * Initializes this place controller with a reference to the context
     * that they can use to access client services and to the
     * configuration record for this place. The controller should create
     * as much of its user interface that it can without having access to
     * the place object because this will be invoked in parallel with the
     * fetching of the place object. When the place object is obtained,
     * the controller will be notified and it can then finish the user
     * interface configuration and put the user interface into operation.
     *
     * @param ctx the client context.
     * @param config the place configuration for this place.
     */
    public void init (CrowdContext ctx, PlaceConfig config)
    {
        // keep these around
        _ctx = ctx;
        _config = config;

        // create our user interface
        _view = createPlaceView();

        // let the derived classes do any initialization stuff
        didInit();
    }

    /**
     * Derived classes can override this and perform any
     * initialization-time processing they might need. They should of
     * course be sure to call <code>super.didInit()</code>.
     */
    protected void didInit ()
    {
    }

    /**
     * Creates the user interface that will be used to display this place.
     * The view instance returned will later be configured with the place
     * object, once it becomes available.
     */
    protected abstract PlaceView createPlaceView ();

    /**
     * This is called by the location director once the place object has
     * been fetched. The place controller will dispatch the place object
     * to the user interface hierarchy via {@link
     * PlaceViewUtil#dispatchWillEnterPlace}. Derived classes can override
     * this and perform any other starting up that they need to do
     */
    public void willEnterPlace (PlaceObject plobj)
    {
        if (_view != null ) {
            // let the UI hierarchy know that we've got our place
            PlaceViewUtil.dispatchWillEnterPlace(_view, plobj);
            // and display the user interface
            _ctx.setPlaceView(_view);
        }
    }

    /**
     * This is called by the location director when we are leaving this
     * place and need to clean up after ourselves and shutdown. Derived
     * classes should override this method (being sure to call
     * <code>super.didLeavePlace</code>) and perform any necessary
     * cleanup.
     */
    public void didLeavePlace (PlaceObject plobj)
    {
        // let the UI hierarchy know that we're outta here
        if (_view != null ) {
            PlaceViewUtil.dispatchDidLeavePlace(_view, plobj);
        }
    }

    /**
     * Handles basic place controller action events. Derived classes
     * should be sure to call <code>super.handleAction</code> for events
     * they don't specifically handle.
     */
    public boolean handleAction (ActionEvent action)
    {
        return false;
    }

    /** A reference to the active client context. */
    protected CrowdContext _ctx;

    /** A reference to our place configuration. */
    protected PlaceConfig _config;

    /** A reference to the place object for which we're controlling a user
     * interface. */
    protected PlaceObject _plobj;

    /** A reference to the root user interface component. */
    protected PlaceView _view;
}
