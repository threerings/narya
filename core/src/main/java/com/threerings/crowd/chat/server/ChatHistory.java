//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.server;

import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.presents.server.PresentsDObjectMgr;
import com.threerings.util.Name;

import com.threerings.crowd.chat.data.ChatChannel;
import com.threerings.crowd.chat.data.ChatMessage;
import com.threerings.crowd.chat.data.KeepNoHistory;
import com.threerings.crowd.chat.data.UserMessage;
import com.threerings.io.Streamable;

/**
 * Provides a server-wide history of chat messages.
 */
@Singleton
public class ChatHistory
{
    /** The amount of time before chat history becomes... history. */
    public static final long HISTORY_EXPIRATION = 5L * 60L * 1000L;

    /**
     * Recorded parcel of chat for historical purposes, maintained by
     * {@link #record(ChatChannel, String, UserMessage, Name[])},
     * {@link #get(Name)}, and {@link #clear(Name)}.
     */
    public static class Entry implements Streamable
    {
        /** The channel on which the message was sent, of null if the channel manager was not
         * used. */
        public final ChatChannel channel;

        /** The message sent. */
        public final ChatMessage message;

        /** The source of the sent message. */
        public final String source;

        public Entry (ChatChannel channel, String source, ChatMessage message) {
            this.channel = channel;
            this.message = message;
            this.source = source;
        }

        public void writeObject (ObjectOutputStream out) throws IOException {
            // ChatMessage.timestamp is transient, so write it externally
            out.defaultWriteObject();
            out.writeLong(message.timestamp);
        }

        public void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            message.timestamp = in.readLong();
        }
    }

    /**
     * Creates a new chat history, automatically registering a message observer with
     * {@link SpeakUtil}.
     */
    public ChatHistory ()
    {
        SpeakUtil.registerMessageObserver(new SpeakUtil.MessageObserver() {
            public void messageDelivered (String source, Name hearer, UserMessage message) {
                record(null, source, message, hearer);
            }
        });
    }

    /**
     * Returns a list of {@link Entry} objects, one for each message to which this user has been
     * privy in the recent past.  If the given name implements {@link KeepNoHistory}, null is
     * returned.
     */
    public List<Entry> get (Name username)
    {
        List<Entry> history = getList(username);
        if (history != null) {
            prune(System.currentTimeMillis(), history);
        }
        return history;
    }

    /**
     * Clears the chat history for the specified user.
     */
    public void clear (final Name username)
    {
        // if we're holding this username for a session observer, postpone until after the current
        // dispatch finishes
        if (_holds.contains(username)) {
            _holds.remove(username);
            _omgr.postRunnable(new Runnable () {
                public void run () {
                    clear(username);
                }
            });
            return;
        }

        // Log.info("Clearing history for " + username + ".");
        _histories.remove(username);
    }

    /**
     * Records the specified channel and message to the specified users' chat histories.  If {@link
     * ChatMessage#timestamp} is not already filled in, it will be.
     */
    public void record (ChatChannel channel, String source, UserMessage msg, Name ...usernames)
    {
        // fill in the message's time stamp if necessary
        if (msg.timestamp == 0L) {
            msg.timestamp = System.currentTimeMillis();
        }

        Entry entry = new Entry(channel, source, msg);
        for (Name username : usernames) {
            // add the message to this user's chat history
            List<Entry> history = getList(username);
            if (history == null) {
                continue;
            }

            history.add(entry);

            // if the history is big enough, potentially prune it (we always prune when asked for
            // the history, so this is just to balance memory usage with CPU expense)
            if (history.size() > 15) {
                prune(msg.timestamp, history);
            }
        }
    }

    /**
     * Causes the chat history for the given user to be held briefly after the {@link #clear}
     * call so that session observers can grab it.
     */
    public void hold (Name username)
    {
        _holds.add(username);
    }

    /**
     * Returns this user's chat history, creating one if necessary. If the given name implements
     * {@link KeepNoHistory}, null is returned.
     */
    protected List<Entry> getList (Name username)
    {
        if (username instanceof KeepNoHistory) {
            return null;
        }
        List<Entry> history = _histories.get(username);
        if (history == null) {
            _histories.put(username, history = Lists.newArrayList());
        }
        return history;
    }

    /**
     * Prunes all messages from this history which are expired.
     */
    protected void prune (long now, List<Entry> history)
    {
        int prunepos = 0;
        for (int ll = history.size(); prunepos < ll; prunepos++) {
            Entry entry = history.get(prunepos);
            if (now - entry.message.timestamp < HISTORY_EXPIRATION) {
                break; // stop when we get to the first valid message
            }
        }
        history.subList(0, prunepos).clear();
    }

    /** Recent chat history for the server. */
    protected Map<Name, List<Entry>> _histories = Maps.newHashMap();

    /** Names we will hold for. */
    protected Set<Name> _holds = Sets.newHashSet();

    // dependencies
    @Inject protected PresentsDObjectMgr _omgr;
}
