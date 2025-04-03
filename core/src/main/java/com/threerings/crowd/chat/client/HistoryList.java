//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.client;

import java.util.ArrayList;

import com.samskivert.util.ObserverList;

import com.threerings.crowd.chat.data.ChatMessage;

/**
 * Stores chat history.
 */
public class HistoryList extends ArrayList<ChatMessage>
    implements ChatDisplay
{
    /** An interface for chat history observers. */
    public interface Observer {
        /** Called when messages have been added or removed from the chat history.
         * @param adjustment the number of messages that have been added (+) or removed (-). */
        void historyUpdated (int adjustment);
    }

    // documentation inherited from interface
    public boolean displayMessage (ChatMessage msg, boolean alreadyDisplayed)
    {
        // see if we're full, and if so, clear out a bunch of old stuff
        int adjusted;
        if (size() == MAX_HISTORY) {
            removeRange(0, PRUNE_HISTORY);
            adjusted = PRUNE_HISTORY;

        } else {
            adjusted = 0;
        }

        // add the message to the history
        add(msg);

        // notify observers that something changed
        notify(adjusted);

        return true;
    }

    @Override
    public void clear ()
    {
        // see how many entries we're clearing out..
        int adjusted = size();
        super.clear();

        // and notify the chat displays of that fact
        notify(adjusted);
    }

    /**
     * Adds an {@link Observer} that wants to know about changes to the history.
     */
    public void addObserver (Observer obs)
    {
        _obs.add(obs);
    }

    /**
     * Removes a {@link Observer} from hearing about changes to the history.
     */
    public void removeObserver (Observer obs)
    {
        _obs.remove(obs);
    }

    /**
     * Notifies listening {@link Observer}s that there has been a change to this history.
     */
    protected void notify (int adjustment)
    {
        _historyUpdatedOp.setAdjustment(adjustment);
        _obs.apply(_historyUpdatedOp);
    }

    protected static class HistoryUpdatedOp
        implements ObserverList.ObserverOp<Observer>
    {
        public void setAdjustment (int adjustment) {
            _adjustment = adjustment;
        }

        public boolean apply (Observer obs) {
            obs.historyUpdated(_adjustment);
            return true;
        }

        protected int _adjustment;
    }

    /** A list of {@link Observer}s interested in history changes. */
    protected ObserverList<Observer> _obs = ObserverList.newFastUnsafe();

    /** An operation used to notify observers of history updates. */
    protected HistoryList.HistoryUpdatedOp _historyUpdatedOp = new HistoryUpdatedOp();

    /** The maximum number of history entries we'll keep. */
    protected static final int MAX_HISTORY = 2000;

    /** The number of history entries we'll prune when we hit the max. */
    protected static final int PRUNE_HISTORY = 200;
}
