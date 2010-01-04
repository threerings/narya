//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.admin.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import com.samskivert.util.ComparableArrayList;

import com.samskivert.swing.ObjectEditorTable;
import com.samskivert.swing.event.CommandEvent;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;

/**
 * Allows simple editing of DSets within a distributed object.
 */
public class DSetEditor<E extends DSet.Entry> extends JPanel
    implements AttributeChangeListener, SetListener<E>, ActionListener
{
    /**
     * Construct a DSet editor to merely display the specified set.
     *
     * @param setter The object that contains the set.
     * @param setName The name of the set in the object.
     * @param entryClass The Class of the DSet.Entry elements contained in the set.
     */
    public DSetEditor (DObject setter, String setName, Class<?> entryClass)
    {
        this(setter, setName, entryClass, null);
    }

    /**
     * Construct a DSetEditor, allowing the specified fields to be edited.
     *
     * @param setter The object that contains the set.
     * @param setName The name of the set in the object.
     * @param entryClass The Class of the DSet.Entry elements contained in the set.
     * @param editableFields the names of the fields in the entryClass that should be editable.
     */
    public DSetEditor (
        DObject setter, String setName, Class<?> entryClass, String[] editableFields)
    {
        this(setter, setName, entryClass, editableFields, null);
    }

    /**
     * Construct a DSetEditor with a custom FieldInterpreter.
     *
     * @param setter The object that contains the set.
     * @param setName The name of the set in the object.
     * @param entryClass the Class of the DSet.Entry elements contained in the set.
     * @param editableFields The names of the fields in the entryClass that should be editable.
     * @param interp The FieldInterpreter to use.
     */
    public DSetEditor (
        DObject setter, String setName, Class<?> entryClass, String[] editableFields,
        ObjectEditorTable.FieldInterpreter interp)
    {
        this(setter, setName, entryClass, editableFields, interp, null, null);
    }

    /**
     * Construct a DSetEditor that only displays entries that match the given Predicate.
     *
     * @param setter The object that contains the set.
     * @param setName The name of the set in the object.
     * @param entryClass The Class of the DSet.Entry elements contained in the set.
     * @param editableFields The names of the fields in the entryClass that should be editable.
     * @param interp The FieldInterpreter to use.
     * @param displayFields The fields to display, or null for all.
     * @param entryFilter The Predicate to use.
     */
    public DSetEditor (
        DObject setter, String setName, Class<?> entryClass, String[] editableFields,
        ObjectEditorTable.FieldInterpreter interp, String[] displayFields, Predicate<E> entryFilter)
    {
        super(new BorderLayout());

        _setter = setter;
        _setName = setName;
        _set = _setter.getSet(setName);
        _entryFilter = entryFilter;

        _table = new ObjectEditorTable(entryClass, editableFields, interp, displayFields);

        add(new JScrollPane(_table), BorderLayout.CENTER);
    }

    /**
     * Get the table being used to display the set.
     */
    public JTable getTable ()
    {
        return _table;
    }

    /**
     * Get the currently selected entry.
     */
    public DSet.Entry getSelectedEntry ()
    {
        return (DSet.Entry)_table.getSelectedObject();
    }

    @Override
    public Dimension getPreferredSize ()
    {
        Dimension d = super.getPreferredSize();
        d.height = Math.min(d.height, MIN_HEIGHT);
        return d;
    }

    @Override
    public void addNotify ()
    {
        super.addNotify();
        _setter.addListener(this);
        _table.addActionListener(this);

        // populate the table
        refreshData();
    }

    @Override
    public void removeNotify ()
    {
        _setter.removeListener(this);
        _table.removeActionListener(this);
        super.removeNotify();
    }

    /**
     * Handles the addition of an entry, assuming our filter allows it.
     */
    protected void addEntry (E entry)
    {
        if (_entryFilter == null || _entryFilter.apply(entry)) {
            int index = _keys.insertSorted(getKey(entry));
            _table.insertDatum(entry, index);
        }
    }

    /**
     * Takes care of removing a key from
     */
    protected void removeKey (Comparable<?> key)
    {
        int index = _keys.indexOf(key);
        if (index != -1) {
            _keys.remove(index);
            _table.removeDatum(index);
        }
    }

    // documentation inherited from interface SetListener
    public void entryAdded (EntryAddedEvent<E> event)
    {
        if (event.getName().equals(_setName)) {
            addEntry(event.getEntry());
        }
    }

    // documentation inherited from interface SetListener
    public void entryRemoved (EntryRemovedEvent<E> event)
    {
        if (event.getName().equals(_setName)) {
            removeKey(event.getKey());
        }
    }

    // documentation inherited from interface SetListener
    public void entryUpdated (EntryUpdatedEvent<E> event)
    {
        if (event.getName().equals(_setName)) {
            E entry = event.getEntry();
            int index = _keys.indexOf(entry.getKey());
            if (index != -1) {
                // We have it, so either update or remove
                if (_entryFilter == null || _entryFilter.apply(entry)) {
                    _table.updateDatum(entry, index);

                } else {
                    removeKey(entry.getKey());
                }
            } else {
                // We DON'T have it, so try to add it in case we care about it
                addEntry(entry);
            }
        }
    }

    // documentation inherited from interface SetListener
    public void attributeChanged (AttributeChangedEvent event)
    {
        if (event.getName().equals(_setName)) {
            // the whole set changed so we need to refetch it from the object
            _set = _setter.getSet(_setName);
            refreshData();
        }
    }

    // documentation inherited from interface ActionListener
    public void actionPerformed (ActionEvent event)
    {
        CommandEvent ce = (CommandEvent)event;
        _setter.updateSet(_setName, (DSet.Entry)ce.getArgument());
    }

    protected void refreshData ()
    {
        _keys = new ComparableArrayList<Comparable<Object>>();
        E[] entries;

        if (_entryFilter == null) {
            @SuppressWarnings("unchecked") E[] tmp = (E[])new DSet.Entry[_set.size()];
            entries = tmp;
            _set.toArray(entries);

        } else {
            // Do some shuffling to get out a filtered array.
            Iterator<E> itr = Iterators.filter(_set.iterator(), _entryFilter);
            ArrayList<E> list = Lists.newArrayList();
            Iterators.addAll(list, itr);

            @SuppressWarnings("unchecked") E[] tmp = (E[])new DSet.Entry[list.size()];
            entries = tmp;
            list.toArray(entries);
        }

        for (E entry : entries) {
            _keys.insertSorted(getKey(entry));
        }
        _table.setData(entries); // this works because DSet itself is sorted
    }

    @SuppressWarnings("unchecked")
    protected static Comparable<Object> getKey (DSet.Entry entry)
    {
        return (Comparable<Object>)entry.getKey();
    }

    /** The object that contains the set we're displaying. */
    protected DObject _setter;

    /** The name of the set in that object. */
    protected String _setName;

    /** An optional predicate to decide whether actually care about displaying a given entry. */
    protected Predicate<E> _entryFilter;

    /** The set itself. */
    protected DSet<E> _set;

    /** An array we use to track our entries' positions by key. */
    protected ComparableArrayList<Comparable<Object>> _keys;

    /** The table used to edit. */
    protected ObjectEditorTable _table;

    /** The minimum height for our editor UI. */
    protected static final int MIN_HEIGHT = 200;
}
