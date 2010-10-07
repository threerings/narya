//
// $Id: $

package com.threerings.admin.web.server;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.RootDObjectManager;

import com.threerings.web.gwt.ServiceException;
import com.threerings.web.server.ServletWaiter;

import com.threerings.admin.server.ConfigRegistry;
import com.threerings.admin.web.gwt.ConfigField;
import com.threerings.admin.web.gwt.ConfigField.FieldType;
import com.threerings.admin.web.gwt.ConfigService;

import static com.threerings.admin.Log.log;

/**
 * Provides the server implementation of {@link ConfigService}.
 */
public abstract class ConfigServlet extends RemoteServiceServlet
    implements ConfigService
{
    // from interface ConfigService
    public ConfigurationResult getConfiguration ()
        throws ServiceException
    {
        requireAdminUser();

        final ServletWaiter<ConfigurationResult> waiter =
            new ServletWaiter<ConfigurationResult>("getConfiguration");

        _omgr.postRunnable(new Runnable() {
            public void run () {
                Map<String, ConfigurationRecord> tabs = Maps.newHashMap();
                for (String key : _confReg.getKeys()) {
                    ConfigurationRecord record = buildRecord(key);
                    if (record == null) {
                        waiter.requestFailed(
                            new ServiceException(InvocationCodes.E_INTERNAL_ERROR));
                        return;
                    }
                    tabs.put(key, record);
                }

                ConfigurationResult result = new ConfigurationResult();
                result.records = tabs;
                waiter.requestCompleted(result);
            }
        });

        return waiter.waitForResult();
    }

    // from interface ConfigService
    public ConfigurationRecord updateConfiguration (final String key, final ConfigField[] updates)
        throws ServiceException
    {
		requireAdminUser();

        final ServletWaiter<ConfigurationRecord> waiter =
            new ServletWaiter<ConfigurationRecord>("updateConfiguration");

        _omgr.postRunnable(new Runnable() {
            public void run () {
                DObject object = _confReg.getObject(key);
                object.startTransaction();

                int updateCount = 0;
                for (ConfigField update : updates) {
                    try {
                        object.changeAttribute(update.name, update.type.toValue(update.valStr));
                        updateCount ++;
                    } catch (ObjectAccessException oae) {
                        log.warning("Failed to update field", "field", update.name, oae);
                    }
                }
                object.commitTransaction();

                ConfigurationRecord record = buildRecord(key);
                record.updates = updateCount;
                waiter.requestCompleted(record);
            }
        });

        return waiter.waitForResult();
    }

    protected ConfigurationRecord buildRecord (String key)
    {
        DObject object = _confReg.getObject(key);
        List<ConfigField> configFields = Lists.newArrayList();
        Field[] fields = object.getClass().getFields();
        for (Field field : fields) {
            if (field.getModifiers() != Modifier.PUBLIC) {
                continue;
            }
            FieldType type = TYPES.get(field.getType());
            if (type == null) {
                log.warning("Unknown field type", "field", field.getName(),
                            "type", field.getType());
                return null;
            }
            try {
                Object value = field.get(object);
                String valStr = type.toString(value);
                configFields.add(new ConfigField(field.getName(), type, valStr));

            } catch (IllegalAccessException e) {
                log.warning("Failure reflecting on configuration object", "key", key,
                    "object", object, "field", field, e);
                return null;
            }
        }
        ConfigurationRecord record = new ConfigurationRecord();
        record.fields = Iterables.toArray(configFields, ConfigField.class);
        return record;
    }

    /**
     * Implemented on a project by project basis to provide a security fence for configuration
     * editing powers.
     */
	protected abstract void requireAdminUser ()
		throws ServiceException;

    @Inject protected ConfigRegistry _confReg;
	@Inject protected RootDObjectManager _omgr;

    protected static Map<Class<?>, FieldType> TYPES = ImmutableMap.<Class<?>, FieldType>builder()
        .put(Integer.class, FieldType.INTEGER)
        .put(Integer.TYPE, FieldType.INTEGER)
        .put(Short.class, FieldType.SHORT)
        .put(Short.TYPE, FieldType.SHORT)
        .put(Long.class, FieldType.LONG)
        .put(Long.TYPE, FieldType.LONG)
        .put(Float.class, FieldType.FLOAT)
        .put(Float.TYPE, FieldType.FLOAT)
        .put(Double.class, FieldType.DOUBLE)
        .put(Double.TYPE, FieldType.DOUBLE)
        .put(Boolean.class, FieldType.BOOLEAN)
        .put(Boolean.TYPE, FieldType.BOOLEAN)
        .put(String.class, FieldType.STRING)
        .build();
}
