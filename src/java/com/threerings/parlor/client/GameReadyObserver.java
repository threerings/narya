//
// $Id: GameReadyObserver.java,v 1.2 2004/08/27 02:20:12 mdb Exp $
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

package com.threerings.parlor.client;

/**
 * Used to inform interested parties when the {@link ParlorDirector}
 * receives a game ready notification. The observers can ratify the
 * decision to head directly into the game or can take responsibility
 * themselves for doing so.
 */
public interface GameReadyObserver
{
    /**
     * Called when a game ready notification is received.
     *
     * @param gameOid the place oid of the ready game.
     *
     * @return if the observer returns true from this method, the parlor
     * director assumes they will take care of entering the game room
     * after performing processing of their own. If all observers return
     * false, the director will enter the game room automatically.
     */
    public boolean receivedGameReady (int gameOid);
}
