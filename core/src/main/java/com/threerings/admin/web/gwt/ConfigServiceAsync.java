//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.web.gwt;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.threerings.admin.web.gwt.ConfigService.ConfigurationRecord;
import com.threerings.admin.web.gwt.ConfigService.ConfigurationResult;

/**
 * Provides the asynchronous version of {@link ConfigService}.
 */
public interface ConfigServiceAsync
{
    /**
     * The async version of {@link ConfigService#getConfiguration}.
     */
    public void getConfiguration (AsyncCallback<ConfigurationResult> callback);

    /**
     * The async version of {@link ConfigService#updateConfiguration}.
     */
    public void updateConfiguration (
        String key, ConfigField[] updates, AsyncCallback<ConfigurationRecord> callback);
}
