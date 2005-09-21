//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

import javax.swing.JComboBox;

import com.samskivert.util.ObjectUtil;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.util.PresentsContext;

import com.threerings.admin.Log;

/**
 * Allows editing of a dobj field using a pulldown.
 */
public class PulldownFieldEditor extends FieldEditor
{
    /**
     * An object that nicely represents a pulldown choice.
     */
    public static class Choice
    {
        /** The value we represent. */
        public Object value;

        /**
         * Construct a pulldown choice.
         */
        public Choice (String displayName, Object value)
        {
            if (displayName == null) {
                throw new NullPointerException("displayName cannot be null.");
            }
            _name = displayName;
            this.value = value;
        }

        // documentation inherited
        public String toString ()
        {
            return _name;
        }

        /** The string representation of this choice. */
        protected String _name;
    }

    /**
     * Construct a PulldownFieldEditor.
     */
    public PulldownFieldEditor (PresentsContext ctx, Field field, DObject obj)
    {
        super(ctx, field, obj);

        add(_value = new JComboBox());
    }

    /**
     * Add a PulldownChoice object as a choice for the pulldown.
     */
    public void addChoice (Choice choice)
    {
        _value.addItem(choice);
    }

    /**
     * Add the specified object as a choice. The name will be the
     * toString() of the object.
     */
    public void addChoice (Object choice)
    {
        String name = (choice == null) ? "null" : choice.toString();
        addChoice(new Choice(name, choice));
    }

    // documentation inherited
    public void addNotify ()
    {
        super.addNotify();
        _value.addActionListener(this);
    }

    // documentation inherited
    public void removeNotify ()
    {
        _value.removeActionListener(this);
        super.removeNotify();
    }

    // documentation inherited
    protected Object getDisplayValue ()
        throws Exception
    {
        Object obj = _value.getSelectedItem();
        if (obj == null) {
            return null;
        }
        return ((Choice) obj).value;
    }

    // documentation inherited
    protected void displayValue (Object value)
    {
        for (int ii = _value.getItemCount() - 1; ii >= 0; ii--) {
            Choice choice = (Choice) _value.getItemAt(ii);
            if (ObjectUtil.equals(value, choice.value)) {
                _value.setSelectedIndex(ii);
                return;
            }
        }

        // cause shit to blow up minorly
        Log.warning("Value in dobj is not settable, disabling choice.");
        Thread.dumpStack();
        _value.setEnabled(false);
    }

    /** Holds the value we're editing. */
    protected JComboBox _value;
}
