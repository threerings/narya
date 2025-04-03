//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.client;

import com.threerings.crowd.data.PlaceObject;

/**
 * This interface provides a convenient means for decoupling user
 * interface components that interact with a place object and that need to
 * keep themselves up to date when the client moves from place to place.
 *
 * <p> In general, such components need to know when the client is about
 * to enter a place so that they can subscribe if necessary or at least
 * extract information about the place. They also need to know when a
 * client has left a place so that they can unsubscribe and clean up after
 * themselves. This is the information that the place view interface makes
 * available to them in a decoupled way.
 *
 * <p> The part of the client implementation that is responsible for the
 * main user interface can act as a location observer, and it can make use
 * of {@link PlaceViewUtil} to dispatch notification of place changes to
 * every <code>PlaceView</code> implementing user interface element in the
 * user interface hierarchy with calls to {@link
 * PlaceViewUtil#dispatchWillEnterPlace} and {@link
 * PlaceViewUtil#dispatchDidLeavePlace}. These functions traverse the UI
 * hierarchy (starting with the element provided which would generally be
 * the top-level UI element, and dispatch calls to {@link #willEnterPlace}
 * and {@link #didLeavePlace} respectively on any UI element they find
 * that implements <code>PlaceView</code>.
 *
 * <p> By doing this, the client code can simply create place-sensitive
 * user interface elements and stick them in the user interface and
 * essentially forget about them, knowing that they will all be notified
 * of place entering and exiting by virtue of the single dispatching
 * calls. It is useful to note that place-sensitive user interface
 * elements will also generally need a reference to the {@link
 * com.threerings.crowd.util.CrowdContext} derivative in use by
 * the client, but those are best supplied at construct time.
 */
public interface PlaceView
{
    /**
     * Called when the client has entered a place and is about to display
     * the user interface for that place.
     *
     * @param plobj the place object that was just entered.
     */
    void willEnterPlace (PlaceObject plobj);

    /**
     * Called after the client has left a place and needs to clean up
     * after the user interface that was displaying that place.
     *
     * @param plobj the place object that was just left.
     */
    void didLeavePlace (PlaceObject plobj);
}
