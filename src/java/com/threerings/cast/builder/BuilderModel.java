//
// $Id: BuilderModel.java,v 1.3 2001/11/18 04:09:21 mdb Exp $

package com.threerings.cast.tools.builder;

import java.util.*;

import com.samskivert.util.CollectionUtil;
import com.samskivert.util.HashIntMap;

import com.threerings.cast.CharacterManager;
import com.threerings.cast.ComponentClass;

/**
 * The builder model represents the current state of the character
 * the user is building.
 */
public class BuilderModel
{
    /**
     * Constructs a builder model.
     */
    public BuilderModel (CharacterManager charmgr)
    {
        gatherComponentInfo(charmgr);
        _selected = new int[_classes.size()];
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
     * Returns a map of the components available for each component
     * class, keyed on component class id.
     */
    public Map getComponents ()
    {
        return Collections.unmodifiableMap(_components);
    }

    /**
     * Returns an array of the currently selected component ids.
     */
    public int[] getSelectedComponents ()
    {
        return _selected;
    }

    /**
     * Sets the selected component for the given component class id.
     */
    public void setSelectedComponent (int clid, int cid)
    {
        _selected[clid] = cid;
        notifyListeners(BuilderModelListener.COMPONENT_CHANGED);
    }

    /**
     * Gathers component class and component information from the
     * character manager for later reference by others.
     */
    protected void gatherComponentInfo (CharacterManager charmgr)
    {
        // get the list of all component classes
        CollectionUtil.addAll(_classes, charmgr.enumerateComponentClasses());

        for (int ii = 0; ii < _classes.size(); ii++) {
            // get the list of components available for this class
            int clid = ((ComponentClass)_classes.get(ii)).clid;
            Iterator iter = charmgr.enumerateComponentsByClass(clid);

            while (iter.hasNext()) {
                Integer cid = (Integer)iter.next();

                ArrayList clist = (ArrayList)_components.get(clid);
                if (clist == null) {
                    _components.put(clid, clist = new ArrayList());
                }

                clist.add(cid);
            }
        }
    }

    /** The currently selected character components. */
    protected int _selected[];

    /** The hashtable of available component ids for each class. */
    protected HashIntMap _components = new HashIntMap();

    /** The list of all available component classes. */
    protected ArrayList _classes = new ArrayList();

    /** The model listeners. */
    protected ArrayList _listeners = new ArrayList();
}
