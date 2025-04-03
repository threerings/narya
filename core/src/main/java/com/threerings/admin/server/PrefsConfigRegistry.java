//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.server;

import com.google.inject.Singleton;

import com.samskivert.util.Config;
import com.samskivert.util.PrefsConfig;

import com.threerings.presents.dobj.DObject;

/**
 * Implements the {@link ConfigRegistry} using the Java preferences system as a persistent store
 * for the configuration information (see {@link Config} for more information on how that works).
 */
@Singleton
public class PrefsConfigRegistry extends ConfigRegistry
{
    @Override // from ConfigRegistry
    protected ObjectRecord createObjectRecord (String path, DObject object)
    {
        return new PrefsObjectRecord(path, object);
    }

    /** Stores preferences using the Java preferences system. */
    protected class PrefsObjectRecord extends ObjectRecord
    {
        public PrefsConfig config;

        public PrefsObjectRecord (String path, DObject object)
        {
            super(object);
            this.config = new PrefsConfig(path);
        }

        @Override
        protected boolean getValue (String field, boolean defval) {
            return config.getValue(field, defval);
        }
        @Override
        protected byte getValue (String field, byte defval) {
            return (byte)config.getValue(field, defval);
        }
        @Override
        protected short getValue (String field, short defval) {
            return (short)config.getValue(field, defval);
        }
        @Override
        protected int getValue (String field, int defval) {
            return config.getValue(field, defval);
        }
        @Override
        protected long getValue (String field, long defval) {
            return config.getValue(field, defval);
        }
        @Override
        protected float getValue (String field, float defval) {
            return config.getValue(field, defval);
        }
        @Override
        protected String getValue (String field, String defval) {
            return config.getValue(field, defval);
        }
        @Override
        protected int[] getValue (String field, int[] defval) {
            return config.getValue(field, defval);
        }
        @Override
        protected float[] getValue (String field, float[] defval) {
            return config.getValue(field, defval);
        }
        @Override
        protected long[] getValue (String field, long[] defval) {
            return config.getValue(field, defval);
        }
        @Override
        protected String[] getValue (String field, String[] defval) {
            return config.getValue(field, defval);
        }

        @Override
        protected void setValue (String field, boolean value) {
            config.setValue(field, value);
        }
        @Override
        protected void setValue (String field, byte value) {
            config.setValue(field, value);
        }
        @Override
        protected void setValue (String field, short value) {
            config.setValue(field, value);
        }
        @Override
        protected void setValue (String field, int value) {
            config.setValue(field, value);
        }
        @Override
        protected void setValue (String field, long value) {
            config.setValue(field, value);
        }
        @Override
        protected void setValue (String field, float value) {
            config.setValue(field, value);
        }
        @Override
        protected void setValue (String field, String value) {
            config.setValue(field, value);
        }
        @Override
        protected void setValue (String field, int[] value) {
            config.setValue(field, value);
        }
        @Override
        protected void setValue (String field, float[] value) {
            config.setValue(field, value);
        }
        @Override
        protected void setValue (String field, long[] value) {
            config.setValue(field, value);
        }
        @Override
        protected void setValue (String field, String[] value) {
            config.setValue(field, value);
        }
    }
}
