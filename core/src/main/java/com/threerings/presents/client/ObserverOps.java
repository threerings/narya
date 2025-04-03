//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

import com.samskivert.util.ObserverList;

/**
 * Used to notify session and client observers.
 */
public class ObserverOps
{
    public abstract static class Session implements ObserverList.ObserverOp<SessionObserver>
    {
        public Session (com.threerings.presents.client.Client client) {
            _client = client;
        }

        public boolean apply (SessionObserver obs) {
            notify(obs);
            return true;
        }

        protected abstract void notify (SessionObserver obs);

        protected com.threerings.presents.client.Client _client;
    }

    public abstract static class Client extends Session
    {
        public Client (com.threerings.presents.client.Client client) {
            super(client);
        }

        @Override public void notify (SessionObserver obs) {
            if (obs instanceof ClientObserver) {
                notify((ClientObserver)obs);
            }
        }

        protected abstract void notify (ClientObserver obs);
    }
}
