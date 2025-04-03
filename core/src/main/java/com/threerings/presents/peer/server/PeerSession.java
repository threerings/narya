//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.peer.server;

import com.google.inject.Inject;

import com.samskivert.util.Throttle;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.net.BootstrapData;
import com.threerings.presents.net.DownstreamMessage;
import com.threerings.presents.net.Message;
import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.peer.net.PeerBootstrapData;
import com.threerings.presents.server.PresentsSession;
import com.threerings.presents.server.net.PresentsConnection;

import static com.threerings.presents.Log.log;

/**
 * Manages a peer session.
 */
public class PeerSession extends PresentsSession
{
    /**
     * Creates a peer session and provides it with a reference to the peer manager.
     */
    @Inject public PeerSession (PeerManager peermgr)
    {
        _peermgr = peermgr;
    }

    /**
     * Passes in a stats instance that this peer can use to note when it trafficks.
     */
    public void setStats (PeerManager.Stats stats)
    {
        _stats = stats;
    }

    @Override // from PresentsSession
    public void handleMessage (Message message)
    {
        super.handleMessage(message);
        if (_stats != null) {
            _stats.notePeerMessageReceived(message);
        }
    }

    @Override // from PresentsSession
    protected BootstrapData createBootstrapData ()
    {
        return new PeerBootstrapData();
    }

    @Override // from PresentsSession
    protected void populateBootstrapData (BootstrapData data)
    {
        super.populateBootstrapData(data);

        // tell our peer about our node object so they can wire up
        PeerBootstrapData pdata = (PeerBootstrapData)data;
        pdata.nodeOid = _peermgr.getNodeObject().getOid();
    }

    @Override // from PresentsSession
    protected void sessionWillStart ()
    {
        super.sessionWillStart();

        // save the client oid so we know it even after the object itself is cleared out
        _cloid = _clobj.getOid();

        // let the peer manager know that we're here
        _peermgr.peerStartedSession(this);
    }

    @Override // from PresentsSession
    protected void sessionConnectionClosed ()
    {
        super.sessionConnectionClosed();

        // if we lose contact with our peer and we have not already ended our session, end it now;
        // we don't want to wait 8 minutes for it to reconnect, there's no need to preserve its
        // session state and there is need to let the peer manager know it's gone ASAP
        if (_clobj != null) {
            log.info("Lost connection to peer, ending session " + this + ".");
            endSession();
        }
    }

    @Override // from PresentsSession
    protected void sessionDidEnd ()
    {
        super.sessionDidEnd();

        // let the peer manager know that we're audi
        _peermgr.peerEndedSession(this);
    }

    @Override // from PresentsSession
    protected final boolean postMessage (DownstreamMessage msg, PresentsConnection expect)
    {
        if (!super.postMessage(msg, expect)) {
            return false;
        }
        if (_stats != null) {
            _stats.notePeerMessageSent(msg);
        }
        return true;
    }

    @Override // from PresentsSession
    protected void subscribedToObject (DObject object)
    {
        super.subscribedToObject(object);
        if (object instanceof NodeObject) {
            _peermgr.clientSubscribedToNode(_cloid);
        }
    }

    @Override // from PresentsSession
    protected void unsubscribedFromObject (DObject object)
    {
        super.unsubscribedFromObject(object);
        if (object instanceof NodeObject) {
            _peermgr.clientUnsubscribedFromNode(_cloid);
        }
    }

    @Override // from PresentsSession
    protected Throttle createIncomingMessageThrottle ()
    {
        // more than 100 messages per second and we complain about it
        return new Throttle(100, 1000L);
    }

    @Override // from PresentsSession
    protected void handleThrottleExceeded ()
    {
        long now = System.currentTimeMillis();
        if (now >= _nextThrottleWarning) {
            log.warning("Peer sent more than 100 messages in one second " + this + ".");
            _nextThrottleWarning = now + 5000L; // don't warn more than once every 5 seconds
        }
    }

    protected PeerManager _peermgr;
    protected PeerManager.Stats _stats;
    protected int _cloid;
    protected long _nextThrottleWarning;
}
