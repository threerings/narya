//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.data;

import com.threerings.crowd.chat.server.SpeakUtil;
import com.threerings.util.Name;

/**
 * Marks a {@link Name} as disinterested in chat history such that {@link SpeakUtil} will keep no
 * messages sent to it.
 */
public interface KeepNoHistory
{
}
