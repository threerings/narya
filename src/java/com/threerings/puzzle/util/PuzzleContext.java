//
// $Id: PuzzleContext.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.util;

import com.threerings.util.KeyDispatcher;
import com.threerings.util.KeyboardManager;

import com.threerings.media.FrameManager;
import com.threerings.media.sound.SoundManager;

import com.threerings.crowd.chat.client.ChatDirector;

/**
 * Provides access to entities needed by the puzzle services.
 */
public interface PuzzleContext
{
    /**
     * Returns the username of the local user.
     */
    public String getUsername ();

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

    /**
     * Provides access to the chat director.
     */
    public ChatDirector getChatDirector ();
}
