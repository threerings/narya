//
// $Id: TrickCardGameManager.java,v 1.2 2004/10/15 00:14:23 andrzej Exp $
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

import com.threerings.parlor.turn.TurnGameManager;

/**
 * A card game manager interface for trick-based card games, such as
 * Spades and Hearts.
 */
public interface TrickCardGameManager extends TurnGameManager
{
    /**
     * Notifies the manager that a hand is about to start.
     */
    public void handWillStart ();
    
    /**
     * Notifies the manager that a hand just started.
     */
    public void handDidStart ();
    
    /**
     * Notifies the manager that a hand has ended.
     */
    public void handDidEnd ();
    
    /**
     * Notifies the manager that a trick is about to start.
     */
    public void trickWillStart ();
    
    /**
     * Notifies the manager that a trick just started.
     */
    public void trickDidStart ();
    
    /**
     * Notifies the manager that a trick has ended.
     */
    public void trickDidEnd ();
}
