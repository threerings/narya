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

package com.threerings.puzzle.util;

import com.threerings.util.KeyDispatcher;
import com.threerings.util.KeyboardManager;
import com.threerings.util.MessageManager;
import com.threerings.util.Name;

import com.threerings.media.FrameManager;
import com.threerings.media.sound.SoundManager;

import com.threerings.parlor.util.ParlorContext;

/**
 * Provides access to entities needed by the puzzle services.
 */
public interface PuzzleContext extends ParlorContext
{
    /**
     * Returns the username of the local user.
     */
    public Name getUsername ();

    /**
     * Returns a reference to the message manager used by the client.
     */
    public MessageManager getMessageManager ();

    /**
     * Provides access to the frame manager.
     */
    public FrameManager getFrameManager ();

    /**
     * Provides access to the keyboard manager.
     */
    public KeyboardManager getKeyboardManager ();

    /**
     * Provides access to the key dispatcher.
     */
    public KeyDispatcher getKeyDispatcher ();

    /**
     * Provides access to the sound manager.
     */
    public SoundManager getSoundManager ();
}
