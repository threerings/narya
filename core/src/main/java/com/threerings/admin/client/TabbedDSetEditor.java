//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import com.samskivert.util.Logger;
import com.samskivert.util.QuickSort;
import com.samskivert.util.StringUtil;

import com.samskivert.swing.ObjectEditorTable;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;

/**
 * Allows simple editing of DSets within a distributed object and easily groups entries into tabs
 * based on the content of some field.
 */
public class TabbedDSetEditor<E extends DSet.Entry> extends JPanel
    implements AttributeChangeListener, SetListener<E>
{
    /**
     * Defines how DSetEditor data-access plugins should be created.
     */
    public interface AccessorFactory
    {
        public <E extends DSet.Entry> DSetEditor.Accessor<E> createAccessor (DSetEditor<E> editor);
    }

    /**
     * Used to divide various entires into different groups.
     */
    public static abstract class EntryGrouper<E extends DSet.Entry>
    {
        /**
         * Subclasses implement the actual logic to figure out a group names from an entry here.
         */
        protected abstract String[] computeGroups (E entry);

        /**
         * Returns a predicate that returns true if the given entry is in the given group.
         */
        protected Predicate<E> getPredicate (final String group) {
            return new Predicate<E>() {
                public boolean apply (E entry) {
                    String[] groups = computeGroups(entry);
                    for (String g: groups) {
                        if (g.equals(group)) {
                            return true;
                        }
                    }
                    return false;
                }
            };
        }

        public void computeGroups (Iterable<E> entries) {
            for (E entry : entries) {
                String[] groups = computeGroups(entry);
                for (String group : groups) {
                    _allGroups.add(group);
                }
            }
        }

        /**
         * Returns all the groups we know about, ordered as they should be displayed.
         */
        public String[] getAllGroups () {
            String[] list = _allGroups.toArray(new String[_allGroups.size()]);
            QuickSort.sort(list, getComparator());

            return list;
        }

        protected Comparator<Object> getComparator () {
            return LEXICAL_CASE_INSENSITIVE;
        }

        protected Set<String> _allGroups = Sets.newHashSet();

        protected static final Comparator<Object> LEXICAL_CASE_INSENSITIVE = Ordering
            .from(String.CASE_INSENSITIVE_ORDER)
            .onResultOf(Functions.toStringFunction())
            .nullsLast();
    }

    public static class FieldGrouper<E extends DSet.Entry> extends EntryGrouper<E>
    {
        public FieldGrouper (String fieldName, Class<?> entryClass) {
            try {
                _field = entryClass.getField(fieldName);
            } catch (NoSuchFieldException nsfe) {
                throw new IllegalArgumentException(Logger.format(
                    "Group field not found in prototype class!",
                    "proto", entryClass, "groupField", fieldName));
            }
        }

        @Override
        protected String[] computeGroups (E entry) {
            try {
                return new String[] { StringUtil.toString(_field.get(entry)) };
            } catch (IllegalAccessException iae) {
                // This ain't good, but let's soldier on.
                return new String[] { "<bogus>" };
            }
        }

        protected final Field _field;
    }

    /**
     * Convenience function to make an edittor that groups based on the values of a given field.
     */
    public TabbedDSetEditor (
        DObject setter, String setName, Class<?> entryClass, String[] editableFields,
        ObjectEditorTable.FieldInterpreter interp, String groupField)
    {
        this(setter, setName, entryClass, editableFields, interp,
            new FieldGrouper<E>(groupField, entryClass));
    }

    /**
     * A set of tabs containing DSetEditors grouping entries by the String value stored in
     * a given field of the Entry.
     */
    public TabbedDSetEditor (
        DObject setter, String setName, Class<?> entryClass, String[] editableFields,
        ObjectEditorTable.FieldInterpreter interp, EntryGrouper<E> grouper)
    {
        // Stash all this for later
        _setter = setter;
        _setName = setName;
        _entryClass = entryClass;
        _editableFields = editableFields;
        _interp = interp;
        _grouper = grouper;

        _tabs = new JTabbedPane();
        add(_tabs);
    }

    /**
     * Assigns the factory that creates data-access plugins for our set our DSetEditors.
     */
    public void setAccessorFactory (AccessorFactory accessorFactory)
    {
        _accessorFactory = accessorFactory;
    }

    protected void computeTabs ()
    {
        _grouper.computeGroups(_setter.<E>getSet(_setName));
        String[] groups = _grouper.getAllGroups();

        for (String group : groups) {
            if (!_editors.containsKey(group)) {
                DSetEditor<E> editor =  createEditor(
                    _setter, _setName, _entryClass, _editableFields, _interp, _grouper, group);
                if (_accessorFactory != null) {
                    editor.setAccessor(_accessorFactory.createAccessor(editor));
                }
                _tabs.add(group, editor);
                _editors.put(group, editor);
            }
        }

        // TODO: Prune any now-empty tabs
    }

    /**
     * Creates a DSetEditor for displaying the given group.
     */
    protected DSetEditor<E> createEditor (
        DObject setter, String setName, Class<?> entryClass, String[] editableFields,
        ObjectEditorTable.FieldInterpreter interp, EntryGrouper<E> grouper, String group)
    {
        return new DSetEditor<E>(setter, setName, entryClass, editableFields,
                interp, getDisplayFields(group), grouper.getPredicate(group));
    }

    /**
     * Choose which fields to display for the given group.
     */
    protected String[] getDisplayFields (String group)
    {
        return null; // Override to display only a subset
    }

    @Override
    public void addNotify ()
    {
        super.addNotify();
        _setter.addListener(this);

        // populate our tabs
        computeTabs();
    }

    @Override
    public void removeNotify ()
    {
        _setter.removeListener(this);
        super.removeNotify();
    }

    public void attributeChanged (AttributeChangedEvent event)
    {
        if (event.getName().equals(_setName)) {
            computeTabs();
        }
    }

    public void entryAdded (EntryAddedEvent<E> event)
    {
        if (event.getName().equals(_setName)) {
            computeTabs();
        }
    }

    public void entryRemoved (EntryRemovedEvent<E> event)
    {
        if (event.getName().equals(_setName)) {
            computeTabs();
        }
    }

    public void entryUpdated (EntryUpdatedEvent<E> event)
    {
        if (event.getName().equals(_setName)) {
            computeTabs();
        }
    }

    protected final DObject _setter;
    protected final String _setName;
    protected final Class<?> _entryClass;
    protected final String[] _editableFields;
    protected final ObjectEditorTable.FieldInterpreter _interp;
    protected final EntryGrouper<E> _grouper;
    protected AccessorFactory _accessorFactory;

    protected JTabbedPane _tabs;
    protected HashMap<String, DSetEditor<E>> _editors = Maps.newHashMap();
}
