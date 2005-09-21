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

package com.threerings.admin.client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Field;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.util.ObjectUtil;
import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.util.PresentsContext;

import com.threerings.admin.Log;

/**
 * Used to display and edit a particular distributed object field.
 */
public abstract class FieldEditor extends JPanel
    implements AttributeChangeListener, ActionListener, FocusListener
{
    public FieldEditor (PresentsContext ctx, Field field, DObject object)
    {
        _ctx = ctx;
        _field = field;
        _object = object;

        // create our interface elements
        setLayout(new HGroupLayout(HGroupLayout.STRETCH));

        // a label to display the field name
        add(_label = new JLabel(_field.getName()));

        // set up our default border
        updateBorder(false);
    }

    // documentation inherited
    public void addNotify ()
    {
        super.addNotify();

        // listen to the object while we're visible
        _object.addListener(FieldEditor.this);
        displayValue(getValue());
    }

    public void removeNotify ()
    {
        super.removeNotify();

        // stop listening when we're hidden
        _object.removeListener(FieldEditor.this);
    }

    // documentation inherited from interface
    public void attributeChanged (AttributeChangedEvent event)
    {
        displayValue(getValue());
        updateBorder(false);
    }

    // documentation inherited from interface
    public void actionPerformed (ActionEvent event)
    {
        Object value = null;
        try {
            value = getDisplayValue();
        } catch (Exception e) {
            updateBorder(true);
        }

        // submit an attribute changed event with the new value
        if (!ObjectUtil.equals(value, getValue())) {
            try {
                _object.changeAttribute(_field.getName(), value);
            } catch (ObjectAccessException oae) {
                Log.warning("Failed to update field " + _field.getName() +
                            ": "+ oae);
            }
        }
    }

    // documentation inherited from interface
    public void focusGained (FocusEvent event)
    {
        // nothing doing
    }

    // documentation inherited from interface
    public void focusLost (FocusEvent event)
    {
        // make sure the value is not changed from the value in the
        // object; if it is, set a modified border
        Object dvalue = null;
        try {
            dvalue = getDisplayValue();
        } catch (Exception e) {
            Log.warning("Failed to parse display value " + e + ".");
            displayValue(getValue());
        }
        updateBorder(!ObjectUtil.equals(dvalue, getValue()));
    }

    /**
     * Returns the currently displayed value.
     */
    protected abstract Object getDisplayValue ()
        throws Exception;

    /**
     * Reads the value from the distributed object field and updates the
     * display with it.
     */
    protected abstract void displayValue (Object value);

    /**
     * Returns the current object value.
     */
    protected Object getValue ()
    {
        try {
            return _field.get(_object);
        } catch (Exception e) {
            Log.warning("Failed to fetch field [field=" + _field +
                        ", object=" + _object + ", error=" + e + "].");
            return null;
        }
    }

    /**
     * Sets the appropriate border on this field editor based on whether
     * or not the field is modified.
     */
    protected void updateBorder (boolean modified)
    {
        if (modified) {
            setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.red));
        } else {
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        }
    }

    protected PresentsContext _ctx;
    protected Field _field;
    protected DObject _object;
    protected JLabel _label;
}
