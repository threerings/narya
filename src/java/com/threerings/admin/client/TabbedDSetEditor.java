//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
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

import java.lang.reflect.Field;
import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import com.samskivert.util.Logger;
import com.samskivert.util.StringUtil;

import com.samskivert.swing.ObjectEditorTable;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;

/**
 * Allows simple editing of DSets within a distributed object and easily groups entries into tabs
 * based on the content of some field.
 */
public class TabbedDSetEditor<E extends DSet.Entry> extends JPanel
{
    /**
     * A set of tabs containing DSetEditors grouping entries by the String value stored in
     * a given field of the Entry.
     */
    public TabbedDSetEditor (
        DObject setter, String setName, Class<?> entryClass,
        String[] editableFields, ObjectEditorTable.FieldInterpreter interp, String groupField)
    {
        DSet<E> set = setter.getSet(setName);

        Field field;

        try {
            field = entryClass.getField(groupField);
        } catch (NoSuchFieldException nsfe) {
            throw new IllegalArgumentException(Logger.format(
                "Group field not found in prototype class!",
                "proto", entryClass, "groupField", groupField));
        }

        HashSet<String> groups = Sets.newHashSet();
        try {
            for (E entry : set) {
                groups.add(StringUtil.toString(field.get(entry)));
            }
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException(Logger.format(
                "Could not access group field on entry", "groupField", groupField, iae));
        }

        JTabbedPane tabs = new JTabbedPane();
        add(tabs);

        for (String group : groups) {
            tabs.addTab(group, new DSetEditor<E>(setter, setName, entryClass, editableFields,
                interp, createPredicate(field, group)));
        }
    }

    protected Predicate<E> createPredicate (final Field field, final String group)
    {
        return new Predicate<E>() {
            public boolean apply (E entry) {
                String val = null;
                try {
                    val = StringUtil.toString(field.get(entry));
                } catch (IllegalAccessException iae) {
                    // Badness, but let's just compare to the null string and be done
                }

                return group.equals(val);
            }
        };
    }
}
