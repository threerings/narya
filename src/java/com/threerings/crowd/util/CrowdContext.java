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

package com.threerings.crowd.util;

import com.threerings.presents.util.PresentsContext;
import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.client.OccupantDirector;
import com.threerings.crowd.client.PlaceView;

import com.threerings.crowd.chat.client.ChatDirector;

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
     * Provides access to the chat director.
     */
    public ChatDirector getChatDirector ();

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
