//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

import java.lang.reflect.Method;

import java.util.HashMap;

import com.google.common.collect.Maps;

import com.samskivert.util.MethodFinder;
import com.samskivert.util.StringUtil;

import static com.threerings.presents.Log.log;

/**
 * Maps distributed object events to methods using reflection.
 */
public class DynamicListener<T extends DSet.Entry>
    implements AttributeChangeListener, ElementUpdateListener, SetListener<T>
{
    /**
     * Creates a listener that dynamically dispatches events on the supplied
     * target.
     */
    public DynamicListener (Object target)
    {
        this(target, new MethodFinder(target.getClass()));
    }

    /**
     * Creates a listener that dynamically dispatches events on the supplied
     * target using the methods in finder.
     */
    public DynamicListener (Object target, MethodFinder finder)
    {
        _target = target;
        _finder = finder;
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
    public void entryAdded (EntryAddedEvent<T> event)
    {
        dispatchMethod(event.getName() + "Added",
            new Object[] { event.getEntry() });
    }

    // from interface SetListener
    public void entryUpdated (EntryUpdatedEvent<T> event)
    {
        dispatchMethod(event.getName() + "Updated",
            new Object[] { event.getEntry() });
    }

    // from interface SetListener
    public void entryRemoved (EntryRemovedEvent<T> event)
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
                log.warning("Failed to dispatch event callback " +
                    name + "(" + StringUtil.toString(arguments) + ").", e);
            }
        }
    }

    /**
     * Looks for a method that matches the supplied signature.
     */
    protected Method resolveMethod (String name, Object[] arguments)
    {
        Class<?>[] ptypes = new Class<?>[arguments.length];
        for (int ii = 0; ii < arguments.length; ii++) {
            ptypes[ii] = arguments[ii] == null ?
                null : arguments[ii].getClass();
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
    protected HashMap<String, Method> _mcache = Maps.newHashMap();
}
