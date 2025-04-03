//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.util;

import com.threerings.util.MessageManager;

import com.threerings.presents.util.PresentsContext;

import com.threerings.crowd.chat.client.ChatDirector;
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
    LocationDirector getLocationDirector ();

    /**
     * Returns a reference to the occupant director.
     */
    OccupantDirector getOccupantDirector ();

    /**
     * Provides access to the chat director.
     */
    ChatDirector getChatDirector ();

    /**
     * Returns a reference to the message manager used by the client to generate localized
     * messages.
     */
    MessageManager getMessageManager ();

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
    void setPlaceView (PlaceView view);

    /**
     * When the client leaves a place, the place controller will remove
     * any place view it set previously via {@link #setPlaceView} with a
     * call to this method.
     */
    void clearPlaceView (PlaceView view);
}
