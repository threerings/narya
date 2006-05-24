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

package com.threerings.cast.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.samskivert.util.CollectionUtil;

import com.threerings.cast.ComponentRepository;
import com.threerings.cast.ComponentClass;

/**
 * The builder model represents the current state of the character the
 * user is building.
 */
public class BuilderModel
{
    /**
     * Constructs a builder model.
     */
    public BuilderModel (ComponentRepository crepo)
    {
        gatherComponentInfo(crepo);
    }

    /**
     * Adds a builder model listener.
     *
     * @param l the listener.
     */
    public void addListener (BuilderModelListener l)
    {
        if (!_listeners.contains(l)) {
            _listeners.add(l);
        }
    }

    /**
     * Notifies all model listeners that the builder model has changed.
     */
    protected void notifyListeners (int event)
    {
	int size = _listeners.size();
	for (int ii = 0; ii < size; ii++) {
	    ((BuilderModelListener)_listeners.get(ii)).modelChanged(event);
	}
    }

    /**
     * Returns a list of the available component classes.
     */
    public List getComponentClasses ()
    {
        return Collections.unmodifiableList(_classes);
    }

    /**
     * Returns the list of components available in the specified class.
     */
    public List getComponents (ComponentClass cclass)
    {
        List list = (List)_components.get(cclass);
        if (list == null) {
            list = new ArrayList();
        }
        return list;
    }

    /**
     * Returns the selected components in an array.
     */
    public int[] getSelectedComponents ()
    {
        int[] values = new int[_selected.size()];
        Iterator iter = _selected.values().iterator();
        for (int i = 0; iter.hasNext(); i++) {
            values[i] = ((Integer)iter.next()).intValue();
        }
        return values;
    }

    /**
     * Sets the selected component for the given component class.
     */
    public void setSelectedComponent (ComponentClass cclass, int cid)
    {
        _selected.put(cclass, Integer.valueOf(cid));
        notifyListeners(BuilderModelListener.COMPONENT_CHANGED);
    }

    /**
     * Gathers component class and component information from the
     * character manager for later reference by others.
     */
    protected void gatherComponentInfo (ComponentRepository crepo)
    {
        // get the list of all component classes
        CollectionUtil.addAll(_classes, crepo.enumerateComponentClasses());

        for (int ii = 0; ii < _classes.size(); ii++) {
            // get the list of components available for this class
            ComponentClass cclass = (ComponentClass)_classes.get(ii);
            Iterator iter = crepo.enumerateComponentIds(cclass);

            while (iter.hasNext()) {
                Integer cid = (Integer)iter.next();
                ArrayList clist = (ArrayList)_components.get(cclass);
                if (clist == null) {
                    _components.put(cclass, clist = new ArrayList());
                }

                clist.add(cid);
            }
        }
    }

    /** The currently selected character components. */
    protected HashMap _selected = new HashMap();

    /** The hashtable of available component ids for each class. */
    protected HashMap _components = new HashMap();

    /** The list of all available component classes. */
    protected ArrayList _classes = new ArrayList();

    /** The model listeners. */
    protected ArrayList _listeners = new ArrayList();
}
