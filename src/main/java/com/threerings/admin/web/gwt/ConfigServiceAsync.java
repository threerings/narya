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
