//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.server;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.HashMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

import com.threerings.presents.dobj.AccessController;
import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.ElementUpdateListener;
import com.threerings.presents.dobj.ElementUpdatedEvent;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;

import static com.threerings.admin.Log.log;

/**
 * Provides a registry of configuration distributed objects. Using distributed object to store
 * runtime configuration data can be exceptionally useful in that clients (with admin privileges)
 * can view and update the running server's configuration parameters on the fly.
 *
 * <p> Users of the service are responsible for creating their own configuration objects which are
 * then registered via this class. The config object registry then performs a few functions:
 *
 * <ul>
 * <li> It populates the config object with values from the persistent configuration information.
 * <li> It mirrors object updates out to the persistent configuration repository.
 * <li> It makes the set of registered objects available for inspection and modification via the
 * admin client interface.
 * </ul>
 *
 * <p> Users of this service will want to use {@link AccessController}s on their configuration
 * distributed objects to prevent non-administrators from subscribing to or modifying the objects.
*/
public abstract class ConfigRegistry
{
    /** Used to un/serialize object data. See {@link #setSerializer}. */
    public static interface Serializer {
        /** Serializes the supplied data into a format that may be saved. */
        String serialize (String name, Object value) throws Exception;

        /** Deserializes the object contained in the specified string. */
        Object deserialize (String value) throws Exception;
    }

    /**
     * Creates a ConfigRegistry that isn't transitioning.
     */
    public ConfigRegistry ()
    {
        this(false);
    }

    /**
     * Creates a ConfigRegistry.
     *
     * @param transitioning if true, serialized Streamable instances stored in the registry will
     * be written back out immediately to allow them to be transitioned to new class names.
     */
    public ConfigRegistry (boolean transitioning)
    {
        _transitioning = transitioning;
    }

    /**
     * Configures the serializer to be used when converting object data to and from its stored
     * representation. This should be done immediately after creation and before any objects are
     * registered.
     */
    public void setSerializer (Serializer serializer) {
        Preconditions.checkState(_configs.isEmpty(),
                                 "Must set serializer before registering config objects.");
        _serializer = serializer;
    }

    /**
     * Registers the supplied configuration object with the system.
     *
     * @param key a string that identifies this object. These are generally hierarchical in nature
     * (of the form <code>system.subsystem</code>), for example: <code>yohoho.crew</code>.
     * @param path The the path in the persistent configuration repository.  This may mean
     * something to the underlying persistent store, for example in the preferences backed
     * implementation it defines the path to the preferences node in the package hierarchy.
     * @param object the object to be registered.
     */
    public void registerObject (String key, String path, DObject object)
    {
        ObjectRecord record = createObjectRecord(path, object);
        record.init();
        _configs.put(key, record);
    }

    /**
     * Returns the config object mapped to the specified key, or null if none exists for that key.
     */
    public DObject getObject (String key)
    {
        ObjectRecord record = _configs.get(key);
        return (record == null) ? null : record.object;
    }

    /**
     * Returns an array containing the keys of all registered configuration objects.
     */
    public String[] getKeys ()
    {
        return _configs.keySet().toArray(new String[_configs.size()]);
    }

    /**
     * Creates an object record derivation that will handle the management of the specified object.
     */
    protected abstract ObjectRecord createObjectRecord (String path, DObject object);

    /**
     * Contains all necessary info for a configuration object registration.
     */
    protected abstract class ObjectRecord
        implements AttributeChangeListener, SetListener<DSet.Entry>, ElementUpdateListener
    {
        public DObject object;

        public ObjectRecord (DObject obj)
        {
            object = obj;
        }

        public void init ()
        {
            // read in the initial configuration settings from the persistent config repository
            Class<?> cclass = object.getClass();
            try {
                Field[] fields = cclass.getFields();
                for (Field field : fields) {
                    int mods = field.getModifiers();
                    if ((mods & Modifier.STATIC) != 0 || (mods & Modifier.PUBLIC) == 0 ||
                        (mods & Modifier.TRANSIENT) != 0) {
                        continue;
                    }
                    initField(field);
                }

                // listen for attribute updates
                object.addListener(this);

            } catch (SecurityException se) {
                log.warning("Unable to reflect on " + cclass.getName() + ": " + se + ". " +
                            "Refusing to monitor object.");
            }
        }

        // from SetListener
        public void entryAdded (EntryAddedEvent<DSet.Entry> event)
        {
            serializeAttribute(event.getName());
        }

        // from SetListener
        public void entryUpdated (EntryUpdatedEvent<DSet.Entry> event)
        {
            serializeAttribute(event.getName());
        }

        // from SetListener
        public void entryRemoved (EntryRemovedEvent<DSet.Entry> event)
        {
            serializeAttribute(event.getName());
        }

        // from ElementUpdateListener
        public void elementUpdated (ElementUpdatedEvent event)
        {
            updateValue(event.getName(), object.getAttribute(event.getName()));
        }

        // from AttributeChangeListener
        public void attributeChanged (AttributeChangedEvent event)
        {
            // mirror this configuration update to the persistent config
            updateValue(event.getName(), event.getValue());
        }

        protected void updateValue (String name, Object value)
        {
            String key = nameToKey(name);
            if (value instanceof Boolean) {
                setValue(key, ((Boolean)value).booleanValue());
            } else if (value instanceof Byte) {
                setValue(key, ((Byte)value).byteValue());
            } else if (value instanceof Short) {
                setValue(key, ((Short)value).shortValue());
            } else if (value instanceof Integer) {
                setValue(key, ((Integer)value).intValue());
            } else if (value instanceof Long) {
                setValue(key, ((Long)value).longValue());
            } else if (value instanceof Float) {
                setValue(key, ((Float)value).floatValue());
            } else if (value instanceof String) {
                setValue(key, (String)value);
            } else if (value instanceof float[]) {
                setValue(key, (float[])value);
            } else if (value instanceof int[]) {
                setValue(key, (int[])value);
            } else if (value instanceof String[]) {
                setValue(key, (String[])value);
            } else if (value instanceof long[]) {
                setValue(key, (long[])value);
            } else if (value == null || Streamer.isStreamable(value.getClass())) {
                serializeAttribute(name);
            } else {
                log.info("Unable to flush config obj change", "cobj", object.getClass().getName(),
                         "key", key, "type", value.getClass().getName(), "value", value);
            }
        }

        /**
         * Initializes a single field of a config distributed object from its corresponding value
         * in the associated config repository.
         */
        protected void initField (Field field)
        {
            String key = nameToKey(field.getName());
            Class<?> type = field.getType();

            try {
                if (type.equals(Boolean.TYPE)) {
                    boolean defval = field.getBoolean(object);
                    field.setBoolean(object, getValue(key, defval));

                } else if (type.equals(Byte.TYPE)) {
                    byte defval = field.getByte(object);
                    field.setByte(object, getValue(key, defval));

                } else if (type.equals(Short.TYPE)) {
                    short defval = field.getShort(object);
                    field.setShort(object, getValue(key, defval));

                } else if (type.equals(Integer.TYPE)) {
                    int defval = field.getInt(object);
                    field.setInt(object, getValue(key, defval));

                } else if (type.equals(Long.TYPE)) {
                    long defval = field.getLong(object);
                    field.setLong(object, getValue(key, defval));

                } else if (type.equals(Float.TYPE)) {
                    float defval = field.getFloat(object);
                    field.setFloat(object, getValue(key, defval));

                } else if (type.equals(String.class)) {
                    String defval = (String)field.get(object);
                    field.set(object, getValue(key, defval));

                } else if (type.equals(int[].class)) {
                    int[] defval = (int[])field.get(object);
                    field.set(object, getValue(key, defval));

                } else if (type.equals(float[].class)) {
                    float[] defval = (float[])field.get(object);
                    field.set(object, getValue(key, defval));

                } else if (type.equals(String[].class)) {
                    String[] defval = (String[])field.get(object);
                    field.set(object, getValue(key, defval));

                } else if (type.equals(long[].class)) {
                    long[] defval = (long[])field.get(object);
                    field.set(object, getValue(key, defval));

                } else if (Streamer.isStreamable(type)) {
                    // don't freak out if the conf is blank.
                    String value = getValue(key, "");
                    if (StringUtil.isBlank(value)) {
                        return;
                    }

                    try {
                        Object deserializedValue = _serializer.deserialize(value);
                        field.set(object, deserializedValue);
                        if (_transitioning) {
                            // Use serialize rather than serializeAttribute so we don't get
                            // ObjectAccessExceptions
                            setValue(nameToKey(key), _serializer.serialize(key, deserializedValue));
                        }
                    } catch (Exception e) {
                        log.warning("Failure decoding config value", "type", type, "field", field,
                                    "exception", e);
                    }

                } else {
                    log.warning("Can't init field of unknown type",
                                "cobj", object.getClass().getName(), "key", key,
                                "type", type.getName());
                }

            } catch (IllegalAccessException iae) {
                log.warning("Can't set field", "cobj", object.getClass().getName(), "key", key,
                            "error", iae);
            }
        }

        /**
         * Get the specified attribute from the configuration object, and serialize it.
         */
        protected void serializeAttribute (String attributeName)
        {
            String key = nameToKey(attributeName);
            Object value = object.getAttribute(attributeName);

            if (value == null || Streamer.isStreamable(value.getClass())) {
                try {
                    setValue(key, _serializer.serialize(attributeName, value));
                } catch (Exception e) {
                    log.warning("Error serializing", "name", attributeName, "value", value);
                }

            } else {
                log.info("Unable to flush config obj change", "cobj", object.getClass().getName(),
                         "key", key, "type", value.getClass().getName(), "value", value);
            }
        }

        /**
         * Converts a config object field name (someConfigMember) to a configuration key
         * (some_config_member).
         */
        protected String nameToKey (String attributeName)
        {
            return StringUtil.unStudlyName(attributeName).toLowerCase();
        }

        protected abstract boolean getValue (String field, boolean defval);
        protected abstract byte getValue (String field, byte defval);
        protected abstract short getValue (String field, short defval);
        protected abstract int getValue (String field, int defval);
        protected abstract long getValue (String field, long defval);
        protected abstract float getValue (String field, float defval);
        protected abstract String getValue (String field, String defval);
        protected abstract int[] getValue (String field, int[] defval);
        protected abstract float[] getValue (String field, float[] defval);
        protected abstract long[] getValue (String field, long[] defval);
        protected abstract String[] getValue (String field, String[] defval);

        protected abstract void setValue (String field, boolean value);
        protected abstract void setValue (String field, byte value);
        protected abstract void setValue (String field, short value);
        protected abstract void setValue (String field, int value);
        protected abstract void setValue (String field, long value);
        protected abstract void setValue (String field, float value);
        protected abstract void setValue (String field, String value);
        protected abstract void setValue (String field, int[] value);
        protected abstract void setValue (String field, float[] value);
        protected abstract void setValue (String field, long[] value);
        protected abstract void setValue (String field, String[] value);
    }

    protected static class DefaultSerializer implements Serializer {
        public String serialize (String name, Object value) throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream oout = createObjectOutputStream(out);
            oout.writeObject(value);
            oout.flush();
            return StringUtil.hexlate(out.toByteArray());
        }

        public Object deserialize (String value) throws Exception {
            ByteArrayInputStream bin = new ByteArrayInputStream(StringUtil.unhexlate(value));
            ObjectInputStream oin = createObjectInputStream(bin);
            return oin.readObject();
        }

        /**
         * Creates an ObjectInputStream to read serialized config entries.
         */
        protected ObjectInputStream createObjectInputStream (InputStream bin) {
            return new ObjectInputStream(bin);
        }

        /**
         * Creates an ObjectOutputStream to write serialized config entries.
         */
        protected ObjectOutputStream createObjectOutputStream (OutputStream bin) {
            return new ObjectOutputStream(bin);
        }
    }

    /** A mapping from identifying key to config object. */
    protected HashMap<String, ObjectRecord> _configs = Maps.newHashMap();

    /** If we need to transition serialized Streamables to a new class format in init.. */
    protected boolean _transitioning;

    /** Used to un/serialize object data. */
    protected Serializer _serializer = new DefaultSerializer();
}
