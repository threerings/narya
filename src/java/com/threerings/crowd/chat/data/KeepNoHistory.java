package com.threerings.crowd.chat.data;

import com.threerings.crowd.chat.server.SpeakUtil;

/**
 * Marks a {@link Name} as disinterested in chat history such that {@link SpeakUtil} will keep no
 * messages sent to it.
 */
public interface KeepNoHistory
{
}
