//
// $Id: PuzzleContext.java,v 1.4 2004/03/06 11:29:19 mdb Exp $

package com.threerings.puzzle.util;

import com.threerings.util.KeyDispatcher;
import com.threerings.util.KeyboardManager;
import com.threerings.util.MessageManager;
import com.threerings.util.Name;

import com.threerings.media.FrameManager;
import com.threerings.media.sound.SoundManager;

import com.threerings.crowd.chat.client.ChatDirector;
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

    /**
     * Provides access to the chat director.
     */
    public ChatDirector getChatDirector ();
}
