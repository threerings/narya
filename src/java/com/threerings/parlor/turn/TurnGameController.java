//
// $Id: TurnGameController.java,v 1.7 2004/08/27 02:20:15 mdb Exp $
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

package com.threerings.parlor.turn;

import com.threerings.util.Name;

/**
 * Games that wish to make use of the turn game services should have their
 * controller implement this interface and create an instance of {@link
 * TurnGameControllerDelegate} which should be passed to {@link
 * GameController#addDelegate}.
 */
public interface TurnGameController
{
    /**
     * Called when the turn changed. This indicates the start of a turn
     * and the user interface should adjust itself accordingly (activating
     * controls if it is our turn and deactivating them if it is not).
     *
     * @param turnHolder the username of the new holder of the turn.
     */
    public void turnDidChange (Name turnHolder);
}
