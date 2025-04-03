//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.client;

import java.lang.reflect.Field;

import javax.swing.JComboBox;

import com.google.common.base.Objects;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.util.PresentsContext;

import static com.threerings.admin.Log.log;

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
        public Choice (String displayName, Object val)
        {
            if (displayName == null) {
                throw new NullPointerException("displayName cannot be null.");
            }
            _name = displayName;
            value = val;
        }

        @Override
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

    @Override
    public void addNotify ()
    {
        super.addNotify();
        _value.addActionListener(this);
    }

    @Override
    public void removeNotify ()
    {
        _value.removeActionListener(this);
        super.removeNotify();
    }

    @Override
    protected Object getDisplayValue ()
        throws Exception
    {
        Object obj = _value.getSelectedItem();
        if (obj == null) {
            return null;
        }
        return ((Choice)obj).value;
    }

    @Override
    protected void displayValue (Object value)
    {
        for (int ii = _value.getItemCount() - 1; ii >= 0; ii--) {
            Choice choice = (Choice)_value.getItemAt(ii);
            if (Objects.equal(value, choice.value)) {
                _value.setSelectedIndex(ii);
                return;
            }
        }

        // cause shit to blow up minorly
        log.warning("Value in dobj is not settable, disabling choice.", new Exception());
        _value.setEnabled(false);
    }

    /** Holds the value we're editing. */
    protected JComboBox _value;
}
