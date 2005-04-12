//
// $Id: CardCodes.java 3224 2004-11-19 19:04:56Z andrzej $
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

package com.threerings.parlor.card.trick.data;

import com.threerings.parlor.card.data.CardCodes;

/**
 * Constants relating to trick-based card games.
 */
public interface TrickCardCodes extends CardCodes
{
    /** For four-player games, the top (opposite) player. */
    public static final int TOP = 0;
    
    /** For four-player games, the bottom (own) player. */
    public static final int BOTTOM = 1;
    
    /** For four-player games, the player on the left. */
    public static final int LEFT = 2;
    
    /** For four-player games, the player on the right. */
    public static final int RIGHT = 3;
}
