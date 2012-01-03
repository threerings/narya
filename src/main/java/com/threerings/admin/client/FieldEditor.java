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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.base.Objects;

import com.samskivert.swing.HGroupLayout;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.util.PresentsContext;

import static com.threerings.admin.Log.log;

/**
 * Used to display and edit a particular distributed object field.
 */
public abstract class FieldEditor extends JPanel
    implements AttributeChangeListener, ActionListener, FocusListener
{
    /** The interface defining how the editor interacts with its data. */
    public interface Accessor
    {
        void added ();
        void removed ();
        void set (Field field, Object value);
        Object get (Field field);
    }

    public FieldEditor (PresentsContext ctx, Field field, DObject object)
    {
        _ctx = ctx;
        _field = field;
        setAccessor(new DObjectAccessor(object));

        // create our interface elements
        setLayout(new HGroupLayout(HGroupLayout.STRETCH));

        // a label to display the field name
        add(_label = new JLabel(_field.getName()));

        // set up our default border
        updateBorder(false);
    }

    /**
     * Sets the plugin for how we interact with our data.
     */
    public void setAccessor (Accessor accessor)
    {
        _accessor = accessor;
    }

    @Override
    public void addNotify ()
    {
        super.addNotify();

        // listen to the object while we're visible
        _accessor.added();
        displayValue(getValue());
    }

    @Override
    public void removeNotify ()
    {
        super.removeNotify();

        // stop listening when we're hidden
        _accessor.removed();
    }

    // documentation inherited from interface
    public void attributeChanged (AttributeChangedEvent event)
    {
        noteUpdatedExternally();
    }

    /** Update ourselves to reflect a change from outside the editor. */
    public void noteUpdatedExternally ()
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
            return;
        }

        // submit an attribute changed event with the new value
        if (!valueMatches(value)) {
            _accessor.set(_field, value);
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
            log.warning("Failed to parse display value " + e + ".");
            displayValue(getValue());
        }

        updateBorder(!valueMatches(dvalue));
    }

    protected boolean valueMatches (Object dvalue)
    {
        return Objects.equal(dvalue, getValue());
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
        return _accessor.get(_field);
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

    /**
     *  A simple accessor that knows how to interact with a DObject - this is normally what is used.
     */
    protected class DObjectAccessor
        implements Accessor
    {
        public DObjectAccessor (DObject obj)
        {
            _obj = obj;
        }

        public void added ()
        {
            _obj.addListener(FieldEditor.this);
        }

        public void removed ()
        {
            _obj.removeListener(FieldEditor.this);
        }

        public void set (Field field, Object value)
        {
            _obj.changeAttribute(field.getName(), value);
        }

        public Object get (Field field)
        {
            try {
                return field.get(_obj);
            } catch (Exception e) {
                log.warning("Failed to fetch field", "field", field, "object", _obj, "error", e);
                return null;
            }
        }

        protected DObject _obj;
    }

    protected PresentsContext _ctx;
    protected Field _field;
    protected Accessor _accessor;
    protected JLabel _label;
}
