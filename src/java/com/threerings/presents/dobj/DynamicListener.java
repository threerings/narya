//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.dobj;

import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.logging.Level;

import com.samskivert.util.MethodFinder;
import com.samskivert.util.StringUtil;

import static com.threerings.presents.Log.log;

/**
 * Maps distributed object events to methods using reflection.
 */
public class DynamicListener
    implements AttributeChangeListener, ElementUpdateListener, SetListener
{
    /**
     * Creates a listener that dynamically dispatches events on the supplied
     * target.
     */
    public DynamicListener (Object target)
    {
        _target = target;
        _finder = new MethodFinder(target.getClass());
    }

    // from interface AttributeChangeListener
    public void attributeChanged (AttributeChangedEvent event)
    {
        dispatchMethod(event.getName() + "Changed",
            new Object[] { event.getValue() });
    }

    // from interface ElementUpdateListener
    public void elementUpdated (ElementUpdatedEvent event)
    {
        dispatchMethod(event.getName() + "Updated",
            new Object[] { event.getIndex(), event.getValue() });
    }

    // from interface SetListener
    public void entryAdded (EntryAddedEvent event)
    {
        dispatchMethod(event.getName() + "Added",
            new Object[] { event.getEntry() });
    }

    // from interface SetListener
    public void entryUpdated (EntryUpdatedEvent event)
    {
        dispatchMethod(event.getName() + "Updated",
            new Object[] { event.getEntry() });
    }

    // from interface SetListener
    public void entryRemoved (EntryRemovedEvent event)
    {
        dispatchMethod(event.getName() + "Removed",
            new Object[] { event.getKey() });
    }

    /**
     * Dynamically looks up the method in question on our target and dispatches
     * an event if it does.
     */
    public void dispatchMethod (String name, Object[] arguments)
    {
        // first check the cache
        Method method = _mcache.get(name);
        if (method == null) {
            // if we haven't already determined this method doesn't exist, try
            // to resolve it
            if (!_mcache.containsKey(name)) {
                _mcache.put(name, method = resolveMethod(name, arguments));
            }
        }
        if (method != null) {
            try {
                method.invoke(_target, arguments);
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to dispatch event callback " +
                    name + "(" + StringUtil.toString(arguments) + ").", e);
            }
        }
    }

    /**
     * Looks for a method that matches the supplied signature.
     */
    protected Method resolveMethod (String name, Object[] arguments)
    {
        Class clazz = _target.getClass();
        Class[] ptypes = new Class[arguments.length];
        for (int ii = 0; ii < arguments.length; ii++) {
            ptypes[ii] = arguments[ii] == null ?
                Object.class : arguments[ii].getClass();
        }
        try {
            return _finder.findMethod(name, ptypes);
        } catch (Exception e) {
            return null;
        }
    }

    /** The object on which we will dynamically dispatch events. */
    protected Object _target;

    /** Used to look up methods. */
    protected MethodFinder _finder;

    /** A cache of already resolved methods. */
    protected HashMap<String,Method> _mcache = new HashMap<String,Method>();
}
