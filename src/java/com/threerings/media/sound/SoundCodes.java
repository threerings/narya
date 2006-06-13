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

package com.threerings.media.sound;

import com.threerings.media.sound.SoundManager.SoundType;

/**
 * A basic set of sound types.
 */
public interface SoundCodes
{
    /**
     * Alert sounds are the type of sounds a player would hear when
     * getting a puzzle challenge.
     */
    public static final SoundType ALERT = new SoundType("alert");

    /**
     * Feedback sounds are the type of sounds a player would here when
     * clicking on buttons or performing an action.
     */
    public static final SoundType FEEDBACK = new SoundType("feedback");

    /**
     * Ambient sounds are birds chirping, waves lapping, boats creaking.
     */
    public static final SoundType AMBIENT = new SoundType("ambient");

    /**
     * Game alert sounds are used to indicate that it's a player's turn.
     */
    public static final SoundType GAME_ALERT = new SoundType("game_alert");

    /**
     * General game sound effects.
     */
    public static final SoundType GAME_FX =new SoundType("game_fx");
}
