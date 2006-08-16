//
// $Id: PlaceController.java 3440 2005-03-30 01:09:30Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.crowd.client  {

import com.threerings.util.Controller;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

/**
 * Controls the user interface that is used to display a place. When the
 * client moves to a new place, the appropriate place controller is
 * constructed and requested to create and display the appopriate user
 * interface for that place.
 */
public /*abstract*/ class PlaceController extends Controller
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
    public function init (ctx :CrowdContext, config :PlaceConfig) :void
    {
        // keep these around
        _ctx = ctx;
        _config = config;

        // create our user interface
        _view = createPlaceView(_ctx);

        // initialize our delegates
        applyToDelegates(function (del :PlaceControllerDelegate) :void {
            del.init(_ctx, _config);
        });

        // let the derived classes do any initialization stuff
        didInit();
    }

    /**
     * Derived classes can override this and perform any
     * post-initialization processing they might need. They should of
     * course be sure to call <code>super.didInit()</code>.
     */
    protected function didInit () :void
    {
    }

    /**
     * Returns a reference to the place view associated with this
     * controller. This is only valid after a call has been made to {@link
     * #init}.
     */
    public function getPlaceView () :PlaceView
    {
        return _view;
    }

    /**
     * Returns the {@link PlaceConfig} associated with this place.
     */
    public function getPlaceConfig () :PlaceConfig
    {
        return _config;
    }

    /**
     * Creates the user interface that will be used to display this place.
     * The view instance returned will later be configured with the place
     * object, once it becomes available.
     *
     * @param ctx a reference to the {@link CrowdContext} associated with
     * this controller.
     */
    protected function createPlaceView (ctx :CrowdContext) :PlaceView
    {
        return null;
    }

    /**
     * This is called by the location director once the place object has
     * been fetched. The place controller will dispatch the place object
     * to the user interface hierarchy via {@link
     * PlaceViewUtil#dispatchWillEnterPlace}. Derived classes can override
     * this and perform any other starting up that they need to do
     */
    public function willEnterPlace (plobj :PlaceObject) :void
    {
        // keep a handle on our place object
        _plobj = plobj;

        if (_view != null ) {
            // set it as our controlled panel
            setControlledPanel(_view);

            // let the UI hierarchy know that we've got our place
            PlaceViewUtil.dispatchWillEnterPlace(_view, plobj);
            // and display the user interface
            _ctx.setPlaceView(_view);
        }

        // let our delegates know what's up 
        applyToDelegates(function (del :PlaceControllerDelegate) :void {
            del.willEnterPlace(plobj);
        });
    }

    /**
     * Called before a request is submitted to the server to leave the
     * current place. As such, this method may be called multiple times
     * before {@link #didLeavePlace} is finally called. The request to
     * leave may be rejected, but if a place controller needs to flush any
     * information to the place manager before it leaves, it should so do
     * here. This is the only place in which the controller is guaranteed
     * to be able to communicate to the place manager, as by the time
     * {@link #didLeavePlace} is called, the place manager may have
     * already been destroyed.
     */
    public function mayLeavePlace (plobj :PlaceObject) :void
    {
        // let our delegates know what's up 
        applyToDelegates(function (del :PlaceControllerDelegate) :void {
            del.mayLeavePlace(plobj);
        });
    }

    /**
     * This is called by the location director when we are leaving this
     * place and need to clean up after ourselves and shutdown. Derived
     * classes should override this method (being sure to call
     * <code>super.didLeavePlace</code>) and perform any necessary
     * cleanup.
     */
    public function didLeavePlace (plobj :PlaceObject) :void
    {
        // let our delegates know what's up 
        applyToDelegates(function (del :PlaceControllerDelegate) :void {
            del.didLeavePlace(plobj);
        });

        setControlledPanel(null);

        // let the UI hierarchy know that we're outta here
        if (_view != null ) {
            PlaceViewUtil.dispatchDidLeavePlace(_view, plobj);
            _ctx.clearPlaceView(_view);
            _view = null;
        }

        _plobj = null;
    }

    /**
     * Handles basic place controller action events. Derived classes
     * should be sure to call <code>super.handleAction</code> for events
     * they don't specifically handle.
     */
    override public function handleAction (cmd :String, arg :Object) :Boolean
    {
        var handled :Boolean = false;

        // let our delegates have a crack at the action
        applyToDelegates(function (del :PlaceControllerDelegate) :void {
            // we take advantage of short-circuiting here
            handled = handled || del.handleAction(cmd, arg);
        });

        // if they didn't handly it, pass it off to the super class
        return handled || super.handleAction(cmd, arg);
    }

    /**
     * Adds the supplied delegate to the list for this controller.
     */
    protected function addDelegate (delegate :PlaceControllerDelegate) :void
    {
        if (_delegates == null) {
            _delegates = new Array();
        }
        _delegates.push(delegate);
    }

    /**
     * Applies the supplied operation to the registered delegates.
     */
    protected function applyToDelegates (visitor :Function) :void
    {
        if (_delegates != null) {
            for (var ii :int = 0; ii < _delegates.length; ii++) {
                visitor.call(this, _delegates[ii]);
            }
        }
    }

    /** A reference to the active client context. */
    protected var _ctx :CrowdContext;

    /** A reference to our place configuration. */
    protected var _config :PlaceConfig;

    /** A reference to the place object for which we're controlling a user
     * interface. */
    protected var _plobj :PlaceObject;

    /** A reference to the root user interface component. */
    protected var _view :PlaceView;

    /** A list of the delegates in use by this controller. */
    protected var _delegates :Array;
}
}
