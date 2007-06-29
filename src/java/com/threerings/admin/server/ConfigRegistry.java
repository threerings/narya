//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.admin.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import javax.swing.JEditorPane;

import com.samskivert.io.ByteArrayOutInputStream;
import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

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
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.SetListener;

import com.threerings.admin.Log;

/**
 * Provides a registry of configuration distributed objects. Using distributed
 * object to store runtime configuration data can be exceptionally useful in
 * that clients (with admin privileges) can view and update the running
 * server's configuration parameters on the fly.
 *
 * <p> Users of the service are responsible for creating their own
 * configuration objects which are then registered via this class. The config
 * object registry then performs a few functions:
 *
 * <ul>

 * <li> It populates the config object with values from the persistent
 * configuration information.
 * <li> It mirrors object updates out to the persistent configuration
 * repository.
 * <li> It makes the set of registered objects available for inspection and
 * modification via the admin client interface.
 * </ul>
 *
 * <p> Users of this service will want to use {@link AccessController}s on
 * their configuration distributed objects to prevent non-administrators from
 * subscribing to or modifying the objects.
*/
public abstract class ConfigRegistry
{
    /**
     * Registers the supplied configuration object with the system.
     *
     * @param key a string that identifies this object. These are generally
     * hierarchical in nature (of the form <code>system.subsystem</code>), for
     * example: <code>yohoho.crew</code>.
     * @param path The the path in the persistent configuration repository.
     * This may mean something to the underlying persistent store, for example
     * in the preferences backed implementation it defines the path to the
     * preferences node in the package hierarchy.
     * @param object the object to be registered.
     */
    public void registerObject (String key, String path, DObject object)
    {
        ObjectRecord record = createObjectRecord(path, object);
        record.init();
        _configs.put(key, record);
    }

    /**
     * Returns the config object mapped to the specified key, or null if none
     * exists for that key.
     */
    public DObject getObject (String key)
    {
        ObjectRecord record = _configs.get(key);
        return (record == null) ? null : record.object;
    }

    /**
     * Returns an array containing the keys of all registered configuration
     * objects.
     */
    public String[] getKeys ()
    {
        return _configs.keySet().toArray(new String[_configs.size()]);
    }

    /**
     * Creates an object record derivation that will handle the management of
     * the specified object.
     */
    protected abstract ObjectRecord createObjectRecord (
        String path, DObject object);

    /**
     * Contains all necessary info for a configuration object registration.
     */
    protected abstract class ObjectRecord
        implements AttributeChangeListener, SetListener, ElementUpdateListener
    {
        public DObject object;

        public ObjectRecord (DObject object)
        {
            this.object = object;
        }

        public void init ()
        {
            // read in the initial configuration settings from the persistent
            // configuration repository
            Class cclass = object.getClass();
            try {
                Field[] fields = cclass.getFields();
                for (int ii = 0; ii < fields.length; ii++) {
                    int mods = fields[ii].getModifiers();
                    if ((mods & Modifier.STATIC) != 0 ||
                        (mods & Modifier.PUBLIC) == 0 ||
                        (mods & Modifier.TRANSIENT) != 0) {
                        continue;
                    }
                    initField(fields[ii]);
                }

                // listen for attribute updates
                object.addListener(this);

            } catch (SecurityException se) {
                Log.warning("Unable to reflect on " + cclass.getName() + ": " +
                            se + ". Refusing to monitor object.");
            }
        }

        public void entryAdded (EntryAddedEvent event)
        {
            serializeAttribute(event.getName());
        }

        public void entryUpdated (EntryUpdatedEvent event)
        {
            serializeAttribute(event.getName());
        }

        public void entryRemoved (EntryRemovedEvent event)
        {
            serializeAttribute(event.getName());
        }
        
        public void elementUpdated (ElementUpdatedEvent event)
        {
            Object value;
            try {
                value = object.getAttribute(event.getName());
            } catch (ObjectAccessException oae) {
                Log.warning("Exception getting field [name=" + event.getName() + "exception=" +
                    oae + "].");
                return;
            }
            updateValue(event.getName(), value);
        }
        
        public void attributeChanged (AttributeChangedEvent event)
        {
            // mirror this configuration update to the persistent config
            Object value = event.getValue();
            if (value instanceof DSet) {
                serializeAttribute(event.getName());
            } else {
                updateValue(event.getName(), value);
            }
        }

        protected void updateValue (String name, Object value)
        {
            String key = nameToKey(name);
            if (value instanceof Boolean) {
                setValue(key, ((Boolean)value).booleanValue());
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
                setValue(key, (long[]) value);
            } else {
                Log.info("Unable to flush config object change " +
                         "[cobj=" + object.getClass().getName() +
                         ", key=" + key +
                         ", type=" + value.getClass().getName() +
                         ", value=" + value + "].");
            }
        }

        /** Initializes a single field of a config distributed object from
         * its corresponding value in the associated config repository. */
        protected void initField (Field field)
        {
            String key = nameToKey(field.getName());
            Class type = field.getType();

            try {
                if (type.equals(Boolean.TYPE)) {
                    boolean defval = field.getBoolean(object);
                    field.setBoolean(object, getValue(key, defval));

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

                } else if (Streamable.class.isAssignableFrom(type)) {
                    // don't freak out if the conf is blank.
                    String value = getValue(key, "");
                    if (StringUtil.isBlank(value)) {
                        return;
                    }

                    try {
                        ByteArrayInputStream bin = new ByteArrayInputStream(
                            StringUtil.unhexlate(value));
                        ObjectInputStream oin = new ObjectInputStream(bin);
                        field.set(object, oin.readObject());
                    } catch (Exception e) {
                        Log.warning("Failure decoding config value [type=" +
                                    type + ", field=" + field + ", exception=" +
                                    e + "].");
                    }

                } else {
                    Log.warning("Can't init field of unknown type " +
                                "[cobj=" + object.getClass().getName() +
                                ", key=" + key +
                                ", type=" + type.getName() + "].");
                }

            } catch (IllegalAccessException iae) {
                Log.warning("Can't set field " +
                            "[cobj=" + object.getClass().getName() +
                            ", key=" + key + ", error=" + iae + "].");
            }
        }

        /**
         * Converts a config object field name (someConfigMember) to a
         * configuration key (some_config_member).
         */
        protected String nameToKey (String attributeName)
        {
            return StringUtil.unStudlyName(attributeName).toLowerCase();
        }

        /**
         * Get the specified attribute from the configuration object, and
         * serialize it.
         */
        protected void serializeAttribute (String attributeName)
        {
            String key = nameToKey(attributeName);
            Object value;
            try {
                value = object.getAttribute(attributeName);
            } catch (ObjectAccessException oae) {
                Log.warning("Exception getting field [name=" + attributeName +
                            "exception=" + oae + "].");
                return;
            }

            if (value instanceof Streamable) {
                serialize(key, value);
            } else {
                Log.info("Unable to flush config object change " +
                         "[cobj=" + object.getClass().getName() +
                         ", key=" + key +
                         ", type=" + value.getClass().getName() +
                         ", value=" + value + "].");
            }
        }

        /**
         * Save the specified object as serialized data associated with
         * the specified key.
         */
        protected void serialize (String key, Object value)
        {
            ByteArrayOutInputStream out = new ByteArrayOutInputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            try {
                oout.writeObject(value);
                oout.flush();
                setValue(key, StringUtil.hexlate(out.toByteArray()));
            } catch (IOException ioe) {
                Log.info("Error serializing value " + value);
            }
        }

        protected abstract boolean getValue (String field, boolean defval);
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

    /** A mapping from identifying key to config object. */
    protected HashMap<String,ObjectRecord> _configs =
        new HashMap<String,ObjectRecord>();
}
