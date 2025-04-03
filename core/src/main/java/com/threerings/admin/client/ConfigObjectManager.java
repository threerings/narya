//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.client;

import java.util.HashMap;

import com.google.common.collect.Maps;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientAdapter;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;

import com.threerings.admin.data.AdminCodes;
import com.threerings.admin.data.ConfigObject;

import static com.threerings.admin.Log.log;

/**
 * Handles subscribing to admin config objects.
 */
public class ConfigObjectManager implements AdminService.ConfigInfoListener
{
    public ConfigObjectManager (Client client)
    {
        _serverconfig = Maps.newHashMap();
        _client = client;
        _client.addClientObserver(new ClientAdapter() {
            @Override
            public void clientWillLogon (Client client) {
                client.addServiceGroup(AdminCodes.ADMIN_GROUP);
            }
            @Override
            public void clientDidLogon (Client client) {
                _dobjmgr = _client.getDObjectManager();
                _service = client.requireService(AdminService.class);
                getConfigInfo();
            }
            @Override
            public void clientDidLogoff (Client client) {
                // Clean up our subscription to the server's configuration
                for (ConfigObjectSubscriber _csubscriber : _csubscribers) {
                    _csubscriber.cleanup();
                }
            }
        });
    }

    /**
     * Returns the ConfigObject identified by the given key.
     */
    public ConfigObject getServerConfig (String key)
    {
        return _serverconfig.get(key);
    }

    // documentation inherited from interface AdminService.ConfigInfoListener
    public void gotConfigInfo (String[] keys, int[] oids)
    {
        _csubscribers = new ConfigObjectSubscriber[keys.length];
        for (int ii = 0; ii < keys.length; ii++) {
            _csubscribers[ii] = new ConfigObjectSubscriber();
            _csubscribers[ii].subscribeConfig(keys[ii], oids[ii]);
        }
    }

    // documentation inherited from interface AdminService.ConfigInfoListener
    public void requestFailed (String reason)
    {
        log.warning("Oh bugger, we didn't get the config data: " + reason);
    }

    /**
     * Convenience: generate a getConfigInfo request to the AdminService from the external class,
     * instead from within the anonymous inner class.
     */
    protected void getConfigInfo ()
    {
        _service.getConfigInfo(this);
    }

    /**
     * This class takes care of the details of subscribing to and placing an individual
     * ConfigObject that the server knows about into a HashMap.
     */
    protected class ConfigObjectSubscriber implements Subscriber<ConfigObject>
    {
        /**
         * This method requests that we place a subscription to the ConfigObject with the given
         * oid, identified by the key; when the object becomes available, it's added to our
         * serverconfig map.
         */
        public void subscribeConfig (String key, int oid) {
            _key = key;
            _oid = oid;
            _dobjmgr.subscribeToObject(_oid, this);
        }

        // documentation inherited from interface Subscriber
        public void objectAvailable (ConfigObject object) {
            _cobj = object;
            _serverconfig.put(_key, _cobj);
        }

        // documentation inherited from interface Subscriber
        public void requestFailed (int oid, ObjectAccessException cause) {
            log.warning("Unable to subscribe to config object " + _key);
        }

        /**
         * Signals that we should stop subscribing to our ConfigObject, and flush out the entry
         * from the serverconfig map.
         */
        public void cleanup () {
            // clear out our subscription
            _dobjmgr.unsubscribeFromObject(_oid, this);
            _cobj = null;
            _serverconfig.remove(_key);
        }

        /** The object that we are tracking. */
        protected ConfigObject _cobj;

        /** The name of the config object to which we are subscribing. */
        protected String _key;

        /** The oid of the object that we're tracking. */
        protected int _oid;
    }

    /** An array of handlers that each subscribe to a single ConfigObject. */
    protected ConfigObjectSubscriber[] _csubscribers;

    /** Our local copy of the server-side runtime configuration. */
    protected HashMap<String, ConfigObject> _serverconfig;

    /** Our distributed object manager. */
    protected DObjectManager _dobjmgr;

    /** Our admin service that we're using to fetch data. */
    protected AdminService _service;

    /** Our client object. */
    protected Client _client;
}
