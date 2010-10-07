//
// $Id: $

package com.threerings.admin.web.client;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.TabPanel;

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
public abstract class ConfigEditorPanel extends TabPanel
    implements ConfigAccessor
{
    public ConfigEditorPanel ()
    {
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
