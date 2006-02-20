//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

import com.samskivert.util.Config;
import com.threerings.presents.dobj.DObject;

/**
 * Implements the {@link ConfigRegistry} using the Java preferences system as a
 * persistent store for the configuration information (see {@link Config} for
 * more information on how that works).
 */
public class PrefsConfigRegistry extends ConfigRegistry
{
    // documentation inherited
    protected ObjectRecord createObjectRecord (String path, DObject object)
    {
        return new PrefsObjectRecord(path, object);
    }

    protected class PrefsObjectRecord extends ObjectRecord
    {
        public Config config;

        public PrefsObjectRecord (String path, DObject object)
        {
            super(object);
            this.config = new Config(path);
        }

        protected boolean getValue (String field, boolean defval) {
            return config.getValue(field, defval);
        }
        protected short getValue (String field, short defval) {
            return (short)config.getValue(field, defval);
        }
        protected int getValue (String field, int defval) {
            return config.getValue(field, defval);
        }
        protected long getValue (String field, long defval) {
            return config.getValue(field, defval);
        }
        protected float getValue (String field, float defval) {
            return config.getValue(field, defval);
        }
        protected String getValue (String field, String defval) {
            return config.getValue(field, defval);
        }
        protected int[] getValue (String field, int[] defval) {
            return config.getValue(field, defval);
        }
        protected float[] getValue (String field, float[] defval) {
            return config.getValue(field, defval);
        }
        protected long[] getValue (String field, long[] defval) {
            return config.getValue(field, defval);
        }
        protected String[] getValue (String field, String[] defval) {
            return config.getValue(field, defval);
        }

        protected void setValue (String field, boolean value) {
            config.setValue(field, value);
        }
        protected void setValue (String field, short value) {
            config.setValue(field, value);
        }
        protected void setValue (String field, int value) {
            config.setValue(field, value);
        }
        protected void setValue (String field, long value) {
            config.setValue(field, value);
        }
        protected void setValue (String field, float value) {
            config.setValue(field, value);
        }
        protected void setValue (String field, String value) {
            config.setValue(field, value);
        }
        protected void setValue (String field, int[] value) {
            config.setValue(field, value);
        }
        protected void setValue (String field, float[] value) {
            config.setValue(field, value);
        }
        protected void setValue (String field, long[] value) {
            config.setValue(field, value);
        }
        protected void setValue (String field, String[] value) {
            config.setValue(field, value);
        }
    }
}
