//
// $Id: Marshaller.java,v 1.10 2002/02/19 03:30:40 mdb Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import com.threerings.presents.Log;
import com.threerings.presents.io.ObjectStreamException;

/**
 * The marshaller inspects the class with which it is constructed and
 * caches the reflection information it needs to marshall and unmarshall
 * instances of that class.
 */
public class Marshaller
{
    /**
     * Writes the supplied object instance out to the supplied data output
     * stream, using a {@link Marshaller} for field marshalling.
     */
    public static void writeObject (DataOutputStream out, Object object)
        throws IOException
    {
        getMarshaller(object).writeTo(out, object);
    }

    /**
     * Populates the fields of the supplied object from the supplied data
     * input stream, using a {@link Marshaller} for field unmarshalling.
     */
    public static void readObject (DataInputStream in, Object object)
        throws IOException
    {
        getMarshaller(object).readFrom(in, object);
    }

    /**
     * Returns the marshaller for objects of this object's class, creating
     * one if necessary.
     */
    protected static synchronized Marshaller getMarshaller (Object object)
    {
        Class clazz = object.getClass();
        Marshaller marsh = (Marshaller)_marshcache.get(clazz);
        if (marsh == null) {
            marsh = new Marshaller(clazz);
            _marshcache.put(clazz, marsh);
        }
        return marsh;
    }

    /**
     * Instantiate a marshaller for the specified class. Introspection is
     * done during construct time to speed up subsequent marshalling and
     * unmarshalling.
     */
    protected Marshaller (Class clazz)
    {
        // we introspect on the class and cache the public data members
        Field[] fields = clazz.getFields();
        ArrayList flist = new ArrayList();

        // we only want non-static, non-final, non-transient fields
        for (int i = 0; i < fields.length; i++) {
            int mods = fields[i].getModifiers();
            if ((mods & Modifier.PUBLIC) == 0 ||
                (mods & Modifier.STATIC) != 0 ||
                (mods & Modifier.TRANSIENT) != 0 ||
                (mods & Modifier.FINAL) != 0) {
                continue;
            }
            flist.add(fields[i]);
        }

        // create an array of the fields we want
        _fields = new Field[flist.size()];
        flist.toArray(_fields);

        // sort the fields so that they are written and read in the same
        // order on all VMs
        Arrays.sort(_fields, FIELD_COMP);

        // now select field marshallers for each of the fields based on
        // their type
        _marshallers = new FieldMarshaller[_fields.length];
        for (int i = 0; i < _fields.length; i++) {
            _marshallers[i] =
                FieldMarshallerRegistry.getMarshaller(_fields[i]);
            // Log.info("Assigned [field=" + _fields[i] +
            // ", marshaller=" + _marshallers[i] + "].");
        }
    }

    /**
     * Writes out all of the fields of the specified distributed object to
     * the supplied data output stream.
     */
    public void writeTo (DataOutputStream out, Object obj)
        throws IOException
    {
        // we simply iterate over our marshallers, writing each field
        // out in succession
        for (int i = 0; i < _fields.length; i++) {
            try {
                _marshallers[i].writeTo(out, _fields[i], obj);

            } catch (IllegalAccessException iae) {
                // this shouldn't happen because we only attempt to marshall
                // public fields, but we'll pass it on none the less
                String errmsg = "Unable to marshall obj field " +
                    "[field=" + _fields[i].getName() +
                    ", obj=" + obj  + "].";
                throw new ObjectStreamException(errmsg, iae);
            }
        }
    }

    /**
     * Reads in all of the fields of the specified distributed object from
     * the supplied data input stream.
     */
    public void readFrom (DataInputStream in, Object obj)
        throws IOException
    {
        // we simply iterate over our marshallers, reading each field in
        // in succession
        for (int i = 0; i < _fields.length; i++) {
            try {
                _marshallers[i].readFrom(in, _fields[i], obj);

            } catch (IllegalAccessException iae) {
                // this shouldn't happen because we only attempt to
                // unmarshall public fields, but we'll pass it on anyway
                String errmsg = "Unable to unmarshall obj field " +
                    "[field=" + _fields[i].getName() +
                    ", obj=" + obj  + "].";
                Log.logStackTrace(iae);
                throw new ObjectStreamException(errmsg, iae);
            }
        }
    }

    /**
     * Used to sort object fields into a predictable order.
     */
    protected static class FieldComparator implements Comparator
    {
        public int compare (Object o1, Object o2)
        {
            return ((Field)o1).getName().compareTo(((Field)o2).getName());
        }

        public boolean equals (Object obj)
        {
            // we don't care about comparing this comparator to others
            return obj == this;
        }
    }

    /** The fields that are marshalled by this marshaller. */
    protected Field[] _fields;

    /** The field marshallers that are used to marshall the fields. */
    protected FieldMarshaller[] _marshallers;

    /** A table of instantiated marshallers for our various marshalled
     * classes. */
    protected static HashMap _marshcache = new HashMap();

    /** We use this to sort the fields into a predictable order. */
    protected static final FieldComparator FIELD_COMP = new FieldComparator();
}
