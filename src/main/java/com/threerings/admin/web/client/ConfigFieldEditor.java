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

package com.threerings.admin.web.client;

import com.google.gwt.user.client.ui.Label;
import com.threerings.admin.web.gwt.ConfigField;
import com.threerings.admin.web.gwt.ConfigField.FieldType;

import com.google.gwt.dom.client.Style;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * A class responsible for constructing and exporting the widgets involved in manipulating
 * one specific configuration field.
 */
public abstract class ConfigFieldEditor
{
    public static ConfigFieldEditor getEditorFor (ConfigField field, Command onChange)
    {
        if (field.type == FieldType.BOOLEAN) {
            return new CheckboxFieldEditor(field, onChange);
        }
        return new StringFieldEditor(field, onChange);
    }

    /**
     * This editor represents values as strings.
     */
    protected static class StringFieldEditor extends ConfigFieldEditor
    {
        public StringFieldEditor (ConfigField field, Command onChange) {
            super(field, onChange);
        }

        @Override
        protected Widget buildWidget (ConfigField field) {
            _box = new TextBox();
            _box.setStyleName("configStringEditor");
            _box.setVisibleLength(40);
            resetField();

            _box.addChangeHandler(new ChangeHandler() {
                public void onChange (ChangeEvent changeEvent) {
                    // if the string fails conversion, just reset to the old value
                    if (_field.type.toValue(_box.getText().trim()) == null) {
                        _box.setText(_field.valStr);
                    }
                    updateModificationState();
                }
            });
            return _box;
        }

        @Override
        public ConfigField getModifiedField () {
            Object newValue = _field.type.toValue(_box.getText().trim());
            if (newValue == null) {
                return null;
            }
            String newValStr = newValue.toString();
            if ((newValStr.length() == 0 && _field.valStr == null)
                    || newValStr.equals(_field.valStr)) {
                return null;
            }
            return new ConfigField(_field.name, _field.type, newValStr);
        }

        @Override
        protected void resetField () {
            _box.setText(_field.valStr);
        }

        protected TextBox _box;
    }

    /**
     * This editor represents boolean values as checkboxes.
     */
    protected static class CheckboxFieldEditor extends ConfigFieldEditor
    {
        public CheckboxFieldEditor (ConfigField field, Command onChange) {
            super(field, onChange);
        }

        @Override
        protected Widget buildWidget (ConfigField field) {
            _box = new CheckBox();
            _box.setStyleName("configCheckBoxEditor");
            resetField();

            _box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                public void onValueChange (ValueChangeEvent<Boolean> changeEvent) {
                    updateModificationState();
                }
            });
            return _box;
        }

        @Override
        public ConfigField getModifiedField () {
            String newValStr = Boolean.toString(_box.getValue());
            if (newValStr.equals(_field.valStr)) {
                return null;
            }
            return new ConfigField(_field.name, _field.type, newValStr);
        }

        @Override
        protected void resetField () {
            _box.setValue(Boolean.valueOf(_field.valStr));
        }

        protected CheckBox _box;
    }

    public ConfigFieldEditor (ConfigField field, Command onChange)
    {
        _field = field;
        _onChange = onChange;

        _value = buildWidget(field);

        _name = new Label(field.name);
        _name.setStyleName("fieldName");

        _reset = new Label("X");
        _reset.setStyleName("resetButton");
        _reset.addClickHandler(new ClickHandler() {
            public void onClick (ClickEvent event) {
                resetField();
                updateModificationState();
            }
        });
        _reset.setVisible(false);
    }

    protected void updateModificationState ()
    {
        Style style = _value.getElement().getStyle();
        if (getModifiedField() != null) {
            style.setBackgroundColor("red");
            _reset.setVisible(true);

        } else {
            style.clearBackgroundColor();
            _reset.setVisible(false);
        }
        _onChange.execute();
    }

    public Widget getNameWidget ()
    {
        return _name;
    }

    public Widget getValueWidget ()
    {
        return _value;
    }

    public Widget getResetWidget ()
    {
        return _reset;
    }

    public abstract ConfigField getModifiedField ();

    protected abstract Widget buildWidget (ConfigField field);
    protected abstract void resetField ();

    protected ConfigField _field;
    protected Command _onChange;

    protected Label _name, _reset;
    protected Widget _value;
}
