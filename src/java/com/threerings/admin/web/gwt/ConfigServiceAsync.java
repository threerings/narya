//
// $Id: $

package com.threerings.admin.web.gwt;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.threerings.admin.web.gwt.ConfigService.ConfigurationRecord;
import com.threerings.admin.web.gwt.ConfigService.ConfigurationResult;

/**
 * Provides the asynchronous version of {@link com.threerings.admin.web.gwt.AdminService}.
 */
public interface ConfigServiceAsync
{
    /**
     * The async version of {@link com.threerings.admin.web.gwt.ConfigService#getConfig}.
     */
    public void getConfiguration (AsyncCallback<ConfigurationResult> callback);

    /**
     * The async version of {@link com.threerings.admin.web.gwt.ConfigService#updateConfiguration}.
     */
    public void updateConfiguration (
        String key, ConfigField[] updates, AsyncCallback<ConfigurationRecord> callback);
}
