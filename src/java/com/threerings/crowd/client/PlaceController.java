//
// $Id$
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

package com.threerings.crowd.client;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
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
public abstract class PlaceController extends Controller
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
        _view = createPlaceView(_ctx);

        // initialize our delegates
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceControllerDelegate delegate) {
                delegate.init(_ctx, _config);
            }
        });

        // let the derived classes do any initialization stuff
        didInit();
    }

    /**
     * Derived classes can override this and perform any
     * post-initialization processing they might need. They should of
     * course be sure to call <code>super.didInit()</code>.
     */
    protected void didInit ()
    {
    }

    /**
     * Returns a reference to the place view associated with this
     * controller. This is only valid after a call has been made to {@link
     * #init}.
     */
    public PlaceView getPlaceView ()
    {
        return _view;
    }

    /**
     * Returns the {@link PlaceConfig} associated with this place.
     */
    public PlaceConfig getPlaceConfig ()
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
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        return createPlaceView();
    }

    /**
     * Obsolete but retained for runtime compatibility with the old and
     * busted.
     *
     * @deprecated Use {@link #createPlaceView(CrowdContext)}.
     */
    protected PlaceView createPlaceView ()
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
    public void willEnterPlace (final PlaceObject plobj)
    {
        // keep a handle on our place object
        _plobj = plobj;

        if (_view != null ) {
            // let the UI hierarchy know that we've got our place
            PlaceViewUtil.dispatchWillEnterPlace(_view, plobj);
            // and display the user interface
            _ctx.setPlaceView(_view);
        }

        // let our delegates know what's up 
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceControllerDelegate delegate) {
                delegate.willEnterPlace(plobj);
            }
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
    public void mayLeavePlace (final PlaceObject plobj)
    {
        // let our delegates know what's up 
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceControllerDelegate delegate) {
                delegate.mayLeavePlace(plobj);
            }
        });
    }

    /**
     * This is called by the location director when we are leaving this
     * place and need to clean up after ourselves and shutdown. Derived
     * classes should override this method (being sure to call
     * <code>super.didLeavePlace</code>) and perform any necessary
     * cleanup.
     */
    public void didLeavePlace (final PlaceObject plobj)
    {
        // let our delegates know what's up 
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceControllerDelegate delegate) {
                delegate.didLeavePlace(plobj);
            }
        });

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
    public boolean handleAction (final ActionEvent action)
    {
        final boolean[] handled = new boolean[1];

        // let our delegates have a crack at the action
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceControllerDelegate delegate) {
                // we take advantage of short-circuiting here
                handled[0] = handled[0] || delegate.handleAction(action);
            }
        });

        // if they didn't handly it, pass it off to the super class
        return handled[0] ? true : super.handleAction(action);
    }

    /**
     * Adds the supplied delegate to the list for this controller.
     */
    protected void addDelegate (PlaceControllerDelegate delegate)
    {
        if (_delegates == null) {
            _delegates = new ArrayList<PlaceControllerDelegate>();
        }
        _delegates.add(delegate);
    }

    /**
     * Used to call methods in delegates.
     */
    protected static interface DelegateOp
    {
        public void apply (PlaceControllerDelegate delegate);
    }

    /**
     * Applies the supplied operation to the registered delegates.
     */
    protected void applyToDelegates (DelegateOp op)
    {
        if (_delegates != null) {
            int dcount = _delegates.size();
            for (int i = 0; i < dcount; i++) {
                op.apply(_delegates.get(i));
            }
        }
    }

    /**
     * Applies the supplied operation to the registered delegates that
     * derive from the specified class.
     */
    protected void applyToDelegates (Class<?> dclass, DelegateOp op)
    {
        if (_delegates != null) {
            int dcount = _delegates.size();
            for (int i = 0; i < dcount; i++) {
                PlaceControllerDelegate delegate = _delegates.get(i);
                if (dclass.isAssignableFrom(delegate.getClass())) {
                    op.apply(delegate);
                }
            }
        }
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

    /** A list of the delegates in use by this controller. */
    protected ArrayList<PlaceControllerDelegate> _delegates;
}
