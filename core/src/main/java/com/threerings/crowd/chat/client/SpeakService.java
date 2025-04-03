//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.client;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;

/**
 * Provides a means by which "speaking" can be allowed among subscribers
 * of a particular distributed object.
 */
public interface SpeakService extends InvocationService<ClientObject>
{
    /**
     * Issues a request to speak "on" the distributed object via which
     * this speak service was provided.
     *
     * @param message the message to be spoken.
     * @param mode the "mode" of the message. This is an opaque value that
     * will be passed back down via the {@link ChatDirector} to the {@link
     * ChatDisplay} implementations which can interpret it in an
     * application specific manner. It's useful for differentiating
     * between regular speech, emotes, etc.
     */
    void speak (String message, byte mode);
}
