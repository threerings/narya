//
// $Id: FieldEditor.java 3286 2004-12-28 03:49:23Z mdb $
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

import java.lang.reflect.Field;
import javax.swing.JTextField;

import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.util.PresentsContext;

import com.threerings.admin.Log;

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

    // documentation inherited
    protected Object getDisplayValue ()
        throws Exception
    {
        String text = _value.getText();
        if (_field.getType().equals(Integer.class) ||
            _field.getType().equals(Integer.TYPE)) {
            return new Integer(text);

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

        } else if (_field.getType().equals(STRING_ARRAY_PROTO.getClass())) {
            return StringUtil.parseStringArray(_value.getText());

        } else if (_field.getType().equals(INT_ARRAY_PROTO.getClass())) {
            return StringUtil.parseIntArray(_value.getText());
            
        } else if (_field.getType().equals(FLOAT_ARRAY_PROTO.getClass())) {
            return StringUtil.parseFloatArray(_value.getText());

        } else if (_field.getType().equals(LONG_ARRAY_PROTO.getClass())) {
            return StringUtil.parseLongArray(_value.getText());
            
        } else if (_field.getType().equals(Boolean.TYPE)) {
            return new Boolean(_value.getText().equalsIgnoreCase("true"));

        } else {
            Log.warning("Unknown field type '" + _field.getName() + "': " +
                        _field.getType().getName() + ".");
            return null;
        }
    }

    // documentation inherited
    protected void displayValue (Object value)
    {
        _value.setText(StringUtil.toString(value, "", ""));
    }

    protected JTextField _value;

    protected static final String[] STRING_ARRAY_PROTO = new String[0];
    protected static final int[] INT_ARRAY_PROTO = new int[0];
    protected static final float[] FLOAT_ARRAY_PROTO = new float[0];
    protected static final long[] LONG_ARRAY_PROTO = new long[0];
}
