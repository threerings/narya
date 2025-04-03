//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.client;

import java.awt.event.ActionEvent;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

/**
 * Provides an extensible mechanism for encapsulating delegated
 * functionality that works with the place services.
 *
 * <p> Thanks to Java's lack of multiple inheritance, it will likely
 * become necessary to factor certain services that might be used by a
 * variety of {@link PlaceController} derived classes into delegate
 * classes because they do not fit into the single inheritance hierarchy
 * that makes sense for a particular application. To facilitate this
 * process, this delegate class is provided which the standard place
 * controller can be made to call out to for all of the standard methods.
 */
public class PlaceControllerDelegate
{
    /**
     * Constructs the delegate with the controller for which it is
     * delegating.
     */
    public PlaceControllerDelegate (PlaceController controller)
    {
        _controller = controller;
    }

    /**
     * Called to initialize the delegate.
     */
    public void init (CrowdContext ctx, PlaceConfig config)
    {
    }

    /**
     * Called to let the delegate know that we're entering a place.
     */
    public void willEnterPlace (PlaceObject plobj)
    {
    }

    /**
     * Called before a request is submitted to the server to leave the
     * current place. The request to leave may be rejected, but if a place
     * controller needs to make a final communication to the place manager
     * before it leaves, it should so do here. This is the only place in
     * which the controller is guaranteed to be able to communicate to the
     * place manager, as by the time {@link #didLeavePlace} is called, the
     * place manager may have already been destroyed.
     */
    public void mayLeavePlace (final PlaceObject plobj)
    {
    }

    /**
     * Called to let the delegate know that we've left the place.
     */
    public void didLeavePlace (PlaceObject plobj)
    {
    }

    /**
     * Called to give the delegate a chance to handle controller actions
     * that weren't handled by the main controller.
     */
    public boolean handleAction (ActionEvent action)
    {
        return false;
    }

    /** A reference to the controller for which we are delegating. */
    protected PlaceController _controller;
}
