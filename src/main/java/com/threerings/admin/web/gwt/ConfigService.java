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
