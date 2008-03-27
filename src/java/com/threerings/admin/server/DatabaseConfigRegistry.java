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

import java.util.HashMap;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.depot.PersistenceContext;
import com.samskivert.util.Invoker;
import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.DObject;

import com.threerings.admin.Log;
import com.threerings.admin.server.persist.ConfigRepository;

/**
 * Implements the {@link ConfigRegistry} using a JDBC database as a persistent store for the
 * configuration information. <em>Note:</em> config objects should only be created during server
 * startup because they will result in synchronous requests to load up the initial configuration
 * data from the database. This ensures that systems initialized after the config registry can
 * safely make use of configuration information.
 */
public class DatabaseConfigRegistry extends ConfigRegistry
{
    /**
     * Creates a configuration registry and prepares it for operation.
     *
     * @param ctx will provide access to our database.
     * @param invoker this will be used to perform all database activity (except first time
     * initialization) so as to avoid blocking the distributed object thread.
     */
    public DatabaseConfigRegistry (PersistenceContext ctx, Invoker invoker)
        throws PersistenceException
    {
        this(ctx, invoker, false);
    }
    
    /**
     * Creates a configuration registry and prepares it for operation.
     * 
     * @param ctx will provide access to our database.
     * @param invoker this will be used to perform all database activity (except first time
     * initialization) so as to avoid blocking the distributed object thread.
     * @param transitioning if the values in the database need to be transitioned to a new format
     */
    public DatabaseConfigRegistry (PersistenceContext ctx, Invoker invoker, boolean transitioning)
        throws PersistenceException
    {
        this(ctx, invoker, "", transitioning);
    }

    /**
     * Creates a configuration registry and prepares it for operation.
     *
     * @param ctx will provide access to our database.
     * @param invoker this will be used to perform all database activity (except first time
     * initialization) so as to avoid blocking the distributed object thread.
     * @param node if this config registry is accessed by multiple servers which wish to maintain
     * separate configs, then specify a node for each server
     */
    public DatabaseConfigRegistry (PersistenceContext ctx, Invoker invoker, String node)
        throws PersistenceException
    {
        this(ctx, invoker, node, false);
    }


    /**
     * Creates a configuration registry and prepares it for operation.
     *
     * @param ctx will provide access to our database.
     * @param invoker this will be used to perform all database activity (except first time
     * initialization) so as to avoid blocking the distributed object thread.
     * @param node if this config registry is accessed by multiple servers which wish to maintain
     * separate configs, then specify a node for each server
     * @param transitioning if the values in the database need to be transitioned to a new format
     */
    public DatabaseConfigRegistry (PersistenceContext ctx, Invoker invoker, String node,
            boolean transitioning)
        throws PersistenceException
    {
        _repo = new ConfigRepository(ctx);
        _invoker = invoker;
        _node = StringUtil.isBlank(node) ? "" : node;
        _transitioning = transitioning;
    }

    @Override // from ConfigRegistry
    protected ObjectRecord createObjectRecord (String path, DObject object)
    {
        return new DatabaseObjectRecord(path, object);
    }

    protected class DatabaseObjectRecord extends ObjectRecord
    {
        public DatabaseObjectRecord (String path, DObject object)
        {
            super(object);
            _path = path;
        }

        public void init ()
        {
            // load up our persistent data synchronously because we should be in the middle of
            // server startup when it's OK to do database access on the main thread and we need to
            // be completely initialized when we return from this call so that subsequent systems
            // can predictably make use of the configuration information that we load
            try {
                _data = _repo.loadConfig(_node, _path, _transitioning);
            } catch (PersistenceException pe) {
                Log.warning("Failed to load object configuration [path=" + _path + "].");
                Log.logStackTrace(pe);
                _data = new HashMap<String,String>();
            }

            super.init();
        }

        protected boolean getValue (String field, boolean defval) {
            String value = _data.get(field);
            if (value != null) {
                return "true".equalsIgnoreCase(value);
            }
            return defval;
        }

        protected short getValue (String field, short defval) {
            String value = _data.get(field);
            try {
                if (value != null) {
                    return Short.parseShort(value);
                }
            } catch (Exception e) {
                // ignore bogus values and return the default
            }
            return defval;
        }

        protected int getValue (String field, int defval) {
            String value = _data.get(field);
            try {
                if (value != null) {
                    return Integer.parseInt(value);
                }
            } catch (Exception e) {
                // ignore bogus values and return the default
            }
            return defval;
        }

        protected long getValue (String field, long defval) {
            String value = _data.get(field);
            try {
                if (value != null) {
                    return Long.parseLong(value);
                }
            } catch (Exception e) {
                // ignore bogus values and return the default
            }
            return defval;
        }

        protected float getValue (String field, float defval) {
            String value = _data.get(field);
            try {
                if (value != null) {
                    return Float.parseFloat(value);
                }
            } catch (Exception e) {
                // ignore bogus values and return the default
            }
            return defval;
        }

        protected String getValue (String field, String defval) {
            String value = _data.get(field);
            return (value == null) ? defval : value;
        }

        protected int[] getValue (String field, int[] defval) {
            String value = _data.get(field);
            try {
                if (value != null) {
                    int[] avalue = StringUtil.parseIntArray(value);
                    if (avalue != null) {
                        return avalue;
                    }
                }
            } catch (Exception e) {
                // ignore bogus values and return the default
            }
            return defval;
        }

        protected float[] getValue (String field, float[] defval) {
            String value = _data.get(field);
            try {
                if (value != null) {
                    float[] avalue = StringUtil.parseFloatArray(value);
                    if (avalue != null) {
                        return avalue;
                    }
                }
            } catch (Exception e) {
                // ignore bogus values and return the default
            }
            return defval;
        }

        protected long[] getValue (String field, long[] defval) {
            String value = _data.get(field);
            try {
                if (value != null) {
                    long[] avalue = StringUtil.parseLongArray(value);
                    if (avalue != null) {
                        return avalue;
                    }
                }
            } catch (Exception e) {
                // ignore bogus values and return the default
            }
            return defval;
        }

        protected String[] getValue (String field, String[] defval) {
            String value = _data.get(field);
            try {
                if (value != null) {
                    return StringUtil.parseStringArray(value);
                }
            } catch (Exception e) {
                // ignore bogus values and return the default
            }
            return defval;
        }

        protected void setValue (String field, boolean value) {
            setAndFlush(field, String.valueOf(value));
        }
        protected void setValue (String field, short value) {
            setAndFlush(field, String.valueOf(value));
        }
        protected void setValue (String field, int value) {
            setAndFlush(field, String.valueOf(value));
        }
        protected void setValue (String field, long value) {
            setAndFlush(field, String.valueOf(value));
        }
        protected void setValue (String field, float value) {
            setAndFlush(field, String.valueOf(value));
        }
        protected void setValue (String field, String value) {
            setAndFlush(field, value);
        }
        protected void setValue (String field, int[] value) {
            setAndFlush(field, StringUtil.toString(value, "", ""));
        }
        protected void setValue (String field, float[] value) {
            setAndFlush(field, StringUtil.toString(value, "", ""));
        }
        protected void setValue (String field, long[] value) {
            setAndFlush(field, StringUtil.toString(value, "", ""));
        }
        protected void setValue (String field, String[] value) {
            setAndFlush(field, StringUtil.joinEscaped(value));
        }

        protected void setAndFlush (final String field, final String value) {
            _data.put(field, value);
            _invoker.postUnit(new Invoker.Unit() {
                public boolean invoke () {
                    try {
                        _repo.updateConfig(_node, _path, field, value);
                    } catch (PersistenceException pe) {
                        Log.warning("Failed to update object configuration [path=" + _path +
                                    ", field=" + field + ", value=" + value + "].");
                        Log.logStackTrace(pe);
                    }
                    return false;
                }
            });
        }

        protected String _path;
        protected HashMap<String,String> _data;
    }

    protected boolean _transitioning;
    protected ConfigRepository _repo;
    protected Invoker _invoker;
    protected String _node;
}
