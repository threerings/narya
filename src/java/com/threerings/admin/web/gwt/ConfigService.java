//
// $Id: $

package com.threerings.admin.web.gwt;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.RemoteService;

import com.threerings.web.gwt.ServiceException;

/**
 * Defines remote services available to admins.
 */
public interface ConfigService extends RemoteService
{
    /**
     * The current runtime configuration of a server, a collection of {@link ConfigurationRecord}
     * objects indexed by key.
     */
    public static class ConfigurationResult
        implements IsSerializable
    {
        public Map<String, ConfigurationRecord> records;
    }

    /**
     * The runtime configuration of a single {@link com.threerings.admin.data.ConfigObject}.
     */
    public static class ConfigurationRecord
        implements IsSerializable
    {
        public ConfigField[] fields;
        public int updates;
    }

    /**
     * Retrieve all the runtime configuration held by the server and return it in a format
     * that is digestible by GWT.
     */
    public ConfigurationResult getConfiguration () throws ServiceException;

    /**
     * Submit a collection of updated fields to the server for application to its runtime
     * configuration. A new snapshot of the configuration state is returned for sanity checking
     * purposes.
     */
    public ConfigurationRecord updateConfiguration (String key, ConfigField[] updates)
        throws ServiceException;
}
