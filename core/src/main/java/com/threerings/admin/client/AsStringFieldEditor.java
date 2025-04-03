//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.client;

import java.lang.reflect.Field;
import javax.swing.JTextField;

import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.util.PresentsContext;

import static com.threerings.admin.Log.log;

/**
 * Used to display and edit a particular distributed object field.
 */
public class AsStringFieldEditor extends FieldEditor
{
    public AsStringFieldEditor (PresentsContext ctx, Field field, DObject object)
    {
        super(ctx, field, object);

        // and a text entry field to display the field value
        add(_value = new JTextField());
        _value.addActionListener(this);

        // we want to let the user know if they remove focus from a text
        // box without changing a field that it's not saved
        _value.addFocusListener(this);
    }

    @Override
    protected Object getDisplayValue ()
        throws Exception
    {
        String text = _value.getText();
        if (_field.getType().equals(Integer.class) ||
            _field.getType().equals(Integer.TYPE)) {
            return new Integer(text);

        } else if (_field.getType().equals(Short.class) ||
            _field.getType().equals(Short.TYPE)) {
            return new Short(text);

        } else if (_field.getType().equals(Byte.class) ||
            _field.getType().equals(Byte.TYPE)) {
            return new Byte(text);

        } else if (_field.getType().equals(Long.class) ||
                   _field.getType().equals(Long.TYPE)) {
            return new Long(text);

        } else if (_field.getType().equals(Float.class) ||
                   _field.getType().equals(Float.TYPE)) {
            return new Float(text);

        } else if (_field.getType().equals(Double.class) ||
                   _field.getType().equals(Double.TYPE)) {
            return new Double(text);

        } else if (_field.getType().equals(String.class)) {
            return text;

        } else if (_field.getType().equals(String[].class)) {
            return StringUtil.parseStringArray(_value.getText());

        } else if (_field.getType().equals(int[].class)) {
            return StringUtil.parseIntArray(_value.getText());

        } else if (_field.getType().equals(float[].class)) {
            return StringUtil.parseFloatArray(_value.getText());

        } else if (_field.getType().equals(long[].class)) {
            return StringUtil.parseLongArray(_value.getText());

        } else if (_field.getType().equals(Boolean.TYPE)) {
            return new Boolean(_value.getText().equalsIgnoreCase("true"));

        } else {
            log.warning("Unknown field type '" + _field.getName() + "': " +
                        _field.getType().getName() + ".");
            return null;
        }
    }

    @Override
    protected void displayValue (Object value)
    {
        _value.setText(StringUtil.toString(value, "", ""));
    }

    @Override
    protected boolean valueMatches (Object dvalue)
    {
        return StringUtil.toString(dvalue).equals(StringUtil.toString(getValue()));
    }

    protected JTextField _value;
}
