//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.presents.peer.server;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.net.BootstrapData;
import com.threerings.presents.server.PresentsClient;

import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.peer.net.PeerBootstrapData;

/**
 * Manages a peer connection.
 */
public class PeerClient extends PresentsClient
{
    @Override // documentation inherited
    public synchronized void mapSubscrip (DObject object)
    {
        super.mapSubscrip(object);
        if (object instanceof NodeObject) {
            _peermgr.clientSubscribedToNode(_cloid);
        }
    }

    @Override // documentation inherited
    public synchronized void unmapSubscrip (int oid)
    {
        if (_subscrips.get(oid) instanceof NodeObject) {
            _peermgr.clientUnsubscribedFromNode(_cloid);
        }
        super.unmapSubscrip(oid);
    }

    /**
     * Creates a peer client and provides it with a reference to the peer
     * manager. This is only done by the {@link PeerClientFactory}.
     */
    protected PeerClient (PeerManager peermgr)
    {
        _peermgr = peermgr;
    }

    /**
     * Derived client classes can override this member to create derived
     * bootstrap data classes that contain extra bootstrap information, if
     * desired.
     */
    protected BootstrapData createBootstrapData ()
    {
        return new PeerBootstrapData();
    }

    /**
     * Derived client classes can override this member to populate the
     * bootstrap data with additional information. They should be sure to
     * call <code>super.populateBootstrapData</code> before doing their
     * own populating, however.
     *
     * <p><em>Note:</em> This function will be called on the dobjmgr
     * thread which means that object manipulations are OK, but client
     * instance manipulations must be done carefully.
     */
    protected void populateBootstrapData (BootstrapData data)
    {
        super.populateBootstrapData(data);

        // tell our peer about our node object so they can wire up
        PeerBootstrapData pdata = (PeerBootstrapData)data;
        pdata.nodeOid = _peermgr.getNodeObject().getOid();
    }

    @Override // documentation inherited
    protected void sessionWillStart ()
    {
        super.sessionWillStart();

        // save the client oid so we know it even after the object itself is cleared out
        _cloid = _clobj.getOid();
    }

    @Override // documentation inherited
    protected void clearSubscrips (boolean verbose)
    {
        for (DObject object : _subscrips.values()) {
            if (object instanceof NodeObject) {
                _peermgr.clientUnsubscribedFromNode(_cloid);
                break;
            }
        }
        super.clearSubscrips(verbose);
    }

    protected PeerManager _peermgr;
    protected int _cloid;
}
