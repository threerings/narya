//
// $Id: FieldEditor.java,v 1.4 2002/09/23 01:31:49 mdb Exp $

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
import javax.swing.event.AncestorEvent;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.event.AncestorAdapter;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.util.PresentsContext;

import com.threerings.admin.Log;

/**
 * Used to display and edit a particular distributed object field.
 */
public class FieldEditor extends JPanel
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

        // and a text entry field to display the field value
        add(_value = new JTextField());
        _value.addActionListener(this);

        // set up our default border
        updateBorder(false);

        // we want to let the user know if they remove focus from a text
        // box without changing a field that it's not saved
        _value.addFocusListener(this);
    }

    // documentation inherited
    public void addNotify ()
    {
        super.addNotify();

        // listen to the object while we're visible
        _object.addListener(FieldEditor.this);
        _value.setText(readValue());
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
        _value.setText(readValue());
        updateBorder(false);
    }

    /**
     * Reads the value from the distributed object field and returns it.
     */
    protected String readValue ()
    {
        try {
            return String.valueOf(_field.get(_object));
        } catch (IllegalAccessException iae) {
            updateBorder(true);
            return "<error>";
        }
    }

    // documentation inherited from interface
    public void actionPerformed (ActionEvent event)
    {
        Object value = null;

        // parse the new value
        if (_field.getType().equals(Integer.class) ||
            _field.getType().equals(Integer.TYPE)) {
            try {
                value = new Integer(_value.getText());
            } catch (NumberFormatException nfe) {
                updateBorder(true);
            }

        } else if (_field.getType().equals(String.class)) {
            value = _value.getText();

        } else if (_field.getType().equals(Boolean.TYPE)) {
            value = new Boolean(_value.getText().equalsIgnoreCase("true"));
        }

        // submit an attribute changed event with the new value
        if (value != null) {
            AttributeChangedEvent ace = new AttributeChangedEvent(
                _object.getOid(), _field.getName(), value);
            _ctx.getDObjectManager().postEvent(ace);
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
        // make sure the value is not changed, if it is, set a modified
        // border
        String value = readValue();
        updateBorder(!value.equals(_value.getText()));
    }

    /**
     * Sets the appropriate border on this field editor based on whether
     * or not the field is modified.
     */
    protected void updateBorder (boolean modified)
    {
        if (modified) {
            _value.setBorder(BorderFactory.createMatteBorder(
                                 2, 2, 2, 2, Color.red));
        } else {
            _value.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        }
    }

    protected PresentsContext _ctx;
    protected Field _field;
    protected DObject _object;
    protected JLabel _label;
    protected JTextField _value;
}
