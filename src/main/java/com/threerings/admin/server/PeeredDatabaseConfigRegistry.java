//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.admin.server;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.Invoker;

import com.samskivert.depot.PersistenceContext;

import com.threerings.io.Streamable;

import com.threerings.util.StreamableTuple;

import com.threerings.presents.annotation.MainInvoker;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.peer.server.PeerManager;

import static com.threerings.admin.Log.log;

/**
 * A database backed config registry that registers with the peer system and synchronizes with its
 * peers when configuration fields are changed.
 */
@Singleton
public class PeeredDatabaseConfigRegistry extends DatabaseConfigRegistry
{
    @Inject public PeeredDatabaseConfigRegistry (
        PersistenceContext ctx, @MainInvoker Invoker invoker, PeerManager peermgr)
    {
        super(ctx, invoker, "");
        _peermgr = peermgr;
    }

    @Override // from ConfigRegistry
    protected ObjectRecord createObjectRecord (String path, DObject object)
    {
        return new PeerDatabaseObjectRecord(path, object);
    }

    /** Stores settings in a database and broadcasts changes to peers. */
    protected class PeerDatabaseObjectRecord extends DatabaseObjectRecord
        implements PeerManager.StaleCacheObserver
    {
        public PeerDatabaseObjectRecord (String path, DObject object)
        {
            super(path, object);
            _peermgr.addStaleCacheObserver(PEER_CACHE_PREFIX + _path, this);
        }

        // from interface PeerManager.StaleCacheObserver
        public void changedCacheData (Streamable data)
        {
            @SuppressWarnings("unchecked") StreamableTuple<String, Object> change =
                (StreamableTuple<String, Object>)data;

            // note that we should ignore the attribute change event we're about to generate
            // because it is not a real configuration change but rather a sync
            try {
                object.changeAttribute(change.left, change.right);
                _pendingSyncs.add(change.left);
            } catch (Exception e) {
                log.warning("Config attribute sync failed " + change + ".", e);
            }
        }

        @Override // from ObjectRecord
        public void attributeChanged (AttributeChangedEvent event)
        {
            // if this was a pending sync event, don't pass it to our parent as it is not a real
            // configuration change event
            if (!_pendingSyncs.remove(event.getName())) {
                super.attributeChanged(event);
            }
        }

        @Override // from ObjectRecord
        protected void updateValue (String name, Object value)
        {
            super.updateValue(name, value);
            fieldUpdated(name, value);
        }

        protected void fieldUpdated (String field, Object value)
        {
            // broadcast to the other nodes that this value has changed
            _peermgr.broadcastStaleCacheData(
                PEER_CACHE_PREFIX + _path, new StreamableTuple<String, Object>(field, value));
        }

        protected ArrayList<String> _pendingSyncs = Lists.newArrayList();
    }

    protected PeerManager _peermgr;

    /** Prefixed to our cache invalidation notifications. */
    protected static final String PEER_CACHE_PREFIX = "PeerConfigCache:";
}
