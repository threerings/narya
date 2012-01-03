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

package com.threerings.admin.web.client;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import com.google.gwt.dom.client.Style;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.TabLayoutPanel;

import com.threerings.admin.web.client.ConfigEditorTab.ConfigAccessor;
import com.threerings.admin.web.gwt.ConfigField;
import com.threerings.admin.web.gwt.ConfigService;
import com.threerings.admin.web.gwt.ConfigService.ConfigurationRecord;
import com.threerings.admin.web.gwt.ConfigService.ConfigurationResult;
import com.threerings.admin.web.gwt.ConfigServiceAsync;
import com.threerings.gwt.util.PopupCallback;

/**
 * The main panel of the configuration editor. All service calls are routed through here.
 * Subclass this class in your project.
 */
public abstract class ConfigEditorPanel extends TabLayoutPanel
    implements ConfigAccessor
{
    /**
     * Create a ConfigEditorPanel with the given bar height, measured in pixels.
     */
    public ConfigEditorPanel (int barPixelHeight)
    {
        this(barPixelHeight, Style.Unit.PX);
    }

    /**
     * Create a ConfigEditorPanel with the given bar height, measured in the given unit.
     */
    public ConfigEditorPanel (int barHeight, Style.Unit unit)
    {
        super(barHeight, unit);

        addStyleName("configEditorPanel");
        ((ServiceDefTarget)_configsvc).setServiceEntryPoint(getServiceEntryPoint());
        _configsvc.getConfiguration(new PopupCallback<ConfigurationResult>() {
            public void onSuccess (ConfigurationResult result) {
                gotData(result);
            }
        });
    }

    public void submitChanges (String key, ConfigField[] modified,
                               AsyncCallback<ConfigurationRecord> callback)
    {
        _configsvc.updateConfiguration(key, modified, callback);
    }

    protected void gotData (ConfigurationResult result)
    {
        clear();

        if (result.records.isEmpty()) {
            return;
        }

        for (Entry<String, ConfigurationRecord> tab : result.records.entrySet()) {
            String tabKey = tab.getKey();
            ConfigEditorTab widget = new ConfigEditorTab(this, tabKey, tab.getValue());
            _tabs.put(tabKey, widget);
            add(widget, tabKey);
        }
        selectTab(0);
    }

    /** Should return the absolute path of the servlet that implements {@link ConfigService}. */
    protected abstract String getServiceEntryPoint();

    protected Map<String, ConfigEditorTab> _tabs = Maps.newHashMap();

    protected static final ConfigServiceAsync _configsvc = GWT.create(ConfigService.class);
}
