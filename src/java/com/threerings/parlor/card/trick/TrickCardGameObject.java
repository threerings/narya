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

package com.threerings.parlor.card.trick;

import com.threerings.parlor.turn.data.TurnGameObject;

/**
 * Game objects for trick-based card games must implement this interface.
 */
public interface TrickCardGameObject extends TurnGameObject
{
    /**
     * Returns the name of the field that signals whether or not the users
     * are currently playing a hand.
     *
     * @return the name of the playingHand field
     */
    public String getPlayingHandFieldName ();
    
    /**
     * Checks whether the users are currently playing a hand.
     *
     * @return true if the users are playing a hand, false otherwise
     */
    public boolean getPlayingHand ();
    
    /**
     * Brings the gameplay in or out of a hand.
     *
     * @param playingHand true to enter a hand, false to leave one
     */
    public void setPlayingHand (boolean playingHand);
    
    /**
     * Returns the name of the field that signals whether or not the users
     * are currently playing a trick.
     *
     * @return the name of the playingTrick field
     */
    public String getPlayingTrickFieldName ();
    
    /**
     * Checks whether the users are currently playing a trick.
     *
     * @return true if the users are playing a trick, false otherwise
     */
    public boolean getPlayingTrick ();
    
    /**
     * Brings the gameplay in or out of a trick.
     *
     * @param playingTrick true to enter a trick, false to leave one
     */
    public void setPlayingTrick (boolean playingTrick);
}
