//
// $Id: SpeakService.java,v 1.2 2003/06/03 21:41:33 ray Exp $

package com.threerings.crowd.chat.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * Provides a means by which "speaking" can be allowed among subscribers
 * of a particular distributed object.
 */
public interface SpeakService extends InvocationService
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
    public void speak (Client client, String message, byte mode);
}
