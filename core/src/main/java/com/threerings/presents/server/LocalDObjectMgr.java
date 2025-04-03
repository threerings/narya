//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import java.awt.EventQueue;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.Subscriber;

/**
 * A special version of the distributed object manager, modified to operate on the AWT thread so
 * that it can run in a client with a GUI and provide a "light" server for local operation of a
 * normally distributed application.
 */
@Singleton
public class LocalDObjectMgr extends PresentsDObjectMgr
{
    /**
     * Creates the dobjmgr and prepares it for operation.
     */
    @Inject public LocalDObjectMgr (ReportManager repmgr)
    {
        super(repmgr);
    }

    /**
     * Creates a {@link DObjectManager} that posts directly to this local object manager, but first
     * sets the source oid of all events to properly identify them with the supplied client oid.
     * Normally this oid setting happens when an event is received on the server over the network,
     * but in local mode we have to do it by hand.
     */
    public DObjectManager getClientDObjectMgr (final int clientOid)
    {
        return new DObjectManager() {
            public boolean isManager (DObject object) {
                return LocalDObjectMgr.this.isManager(object);
            }
            public <T extends DObject> void subscribeToObject (int oid, Subscriber<T> target) {
                LocalDObjectMgr.this.subscribeToObject(oid, target);
            }
            public <T extends DObject> void unsubscribeFromObject (int oid, Subscriber<T> target) {
                LocalDObjectMgr.this.unsubscribeFromObject(oid, target);
            }
            public void postEvent (DEvent event) {
                event.setSourceOid(clientOid);
                LocalDObjectMgr.this.postEvent(event);
            }
            public void removedLastSubscriber (DObject obj, boolean deathWish) {
                LocalDObjectMgr.this.removedLastSubscriber(obj, deathWish);
            }
        };
    }

    @Override
    public synchronized boolean isDispatchThread ()
    {
        return EventQueue.isDispatchThread();
    }

    @Override
    public void postEvent (final DEvent event)
    {
        EventQueue.invokeLater(new Runnable() {
            public void run () {
                processUnit(event);
            }
        });
    }

    @Override
    public void postRunnable (Runnable unit)
    {
        // we just pass this right on to the AWT event queue rather than running them through
        // processUnit() which would basically just call run() though we lose a tiny bit of
        // inconsequential accounting data
        EventQueue.invokeLater(unit);
    }
}
