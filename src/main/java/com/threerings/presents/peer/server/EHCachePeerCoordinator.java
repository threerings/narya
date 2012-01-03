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

package com.threerings.presents.peer.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.samskivert.util.Tuple;

import com.threerings.presents.peer.data.NodeObject;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.CacheManagerPeerProviderFactory;
import net.sf.ehcache.distribution.CachePeer;

import static com.threerings.presents.Log.log;

/**
 * An EHCache peer coordinator that obtains its information from the Narya peer system.
 */
public class EHCachePeerCoordinator extends CacheManagerPeerProviderFactory
{
    /** Must correspond to what's provided to the PeerManagerCacheListener in ehcache.xml. */
    public static final int RMI_PORT = 40001;

    public static void initWithPeers (PeerManager peerMan)
    {
        if (_instance == null) {
            log.warning("No provider initialized -- not coordinating Presents and EHCache peers.");
            return;
        }
        _instance.initWithPeers(peerMan);
    }

    /** Return our provider, creating it if needed. */
    @Override
    public CacheManagerPeerProvider createCachePeerProvider (
        CacheManager cacheManager, Properties properties)
    {
        if (_instance == null) {
            _instance = new Provider(cacheManager);
        }
        return _instance;
    }

    protected static class Provider implements CacheManagerPeerProvider
    {
        public Provider (CacheManager cacheManager)
        {
            _cacheMan = cacheManager;
        }

        public void initWithPeers (PeerManager peerMan)
        {
            _peerMan = peerMan;
        }

        // these are internal to EHCache and I have NO clue why they're in the API
        public void registerPeer (String rmiUrl) { }
        public void unregisterPeer (String rmiUrl) { }

        public List<?> listRemoteCachePeers (Ehcache cache)
            throws CacheException
        {
            if (_peerMan == null) {
                // the ehcache subsystem has fired up but the server is still booting; we return
                // empty here and ehcache will try again
                return Collections.emptyList();
            }

            // list the current peers
            final List<CachePeer> result = Lists.newArrayList();
            final Set<String> nodes = Sets.newHashSet();
            for (NodeObject node : _peerMan.getNodeObjects()) {
                if (node != _peerMan.getNodeObject()) {
                    addCacheForNode(result, node.nodeName, cache.getName());
                    nodes.add(node.nodeName);
                }
            }

            // if any previously known peer is no longer with us, clear out the cache
            Set<Tuple<String, String>> toRemove = Sets.newHashSet();
            for (Tuple<String, String> key : _peerCache.keySet()) {
                if (!nodes.contains(key.left)) {
                    toRemove.add(key);
                }
            }
            for (Tuple<String, String> key : toRemove) {
                log.info("Removing EHCache peer: " + key);
                _peerCache.remove(key);
            }

            return result;
        }

        public void init ()
        {
            // do nothing
        }

        public void dispose ()
            throws CacheException
        {
            // do nothing
        }

        public long getTimeForClusterToForm ()
        {
            // this is only used when bootstrapping, which we don't do, but whatever
            return 10000;
        }

        public String getScheme ()
        {
            return "RMI";
        }

        protected void addCacheForNode (List<CachePeer> result, String nodeName, String cacheName)
        {
            String nodeHost = _peerMan.getPeerInternalHostName(nodeName);
            if (nodeHost == null) {
                log.warning("Eek, couldn't find the public host name of peer", "node", nodeName);
                return;
            }
            try {
                String rmiBase = "//" + nodeHost + ":" + RMI_PORT + "/";
                result.add(getCache(nodeName, rmiBase + cacheName));
            } catch (Exception e) {
                log.warning("Could not resolve remote peer", "host", nodeHost,
                            "cache", cacheName, e);
            }
        }

        protected CachePeer getCache (String nodeName, String url)
            throws MalformedURLException, RemoteException, NotBoundException
        {
            Tuple<String, String> key = Tuple.newTuple(nodeName, url);
            // retrieve the RMI handle for the given peer
            synchronized(_peerCache) {
                CachePeer peer = _peerCache.get(key);
                if (peer == null) {
                    // do the (blocking) lookup and stow away the result
                    log.info("RMI lookup of remote cache", "url", url);
                    peer = (CachePeer) Naming.lookup(url);
                    _peerCache.put(key, peer);
                }
                return peer;
            }
        }

        protected Map<Tuple<String, String>, CachePeer> _peerCache = Maps.newHashMap();
        protected PeerManager _peerMan;
        protected CacheManager _cacheMan;
    }

    protected static Provider _instance;
}
