//
// $Id: ButtonSpriteObserver.java,v 1.1 2004/10/29 00:41:50 andrzej Exp $
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

package com.threerings.parlor.card.client;

import com.samskivert.swing.event.CommandEvent;

/**
 * Observer interface for button sprites.
 */
public interface ButtonSpriteObserver
{
    /**
     * Notifies the observer that the user clicked a button sprite.
     *
     * @param sprite the clicked sprite
     * @param ce the command event associated with the click
     */
    public void buttonSpriteClicked (ButtonSprite sprite, CommandEvent ce);
}
