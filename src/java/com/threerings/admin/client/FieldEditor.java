//
// $Id: FieldEditor.java,v 1.2 2002/06/07 17:34:37 mdb Exp $

package com.threerings.admin.client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    implements AttributeChangeListener, ActionListener
{
    public FieldEditor (PresentsContext ctx, Field field, DObject object)
    {
        _ctx = ctx;
        _field = field;
        _object = object;

        // create our interface elements
        setLayout(new HGroupLayout(HGroupLayout.STRETCH));

        // set up our default border
        updateBorder(false);

        // a label to display the field name
        add(_label = new JLabel(_field.getName()), HGroupLayout.FIXED);

        // and a text entry field to display the field value
        add(_value = new JTextField());
        _value.addActionListener(this);

        // listen while we're shown and not when we're not
        addAncestorListener(new AncestorAdapter () {
            public void ancestorAdded (AncestorEvent event) {
                _object.addListener(FieldEditor.this);
                readValue();
            }
            public void ancestorRemoved (AncestorEvent event) {
                _object.removeListener(FieldEditor.this);
            }
        });
    }

    // documentation inherited from interface
    public void attributeChanged (AttributeChangedEvent event)
    {
        readValue();
    }

    /**
     * Reads the value from the distributed object field and updates the
     * editor field appropriately.
     */
    protected void readValue ()
    {
        String value = "";
        try {
            value = String.valueOf(_field.get(_object));
            updateBorder(false);
        } catch (IllegalAccessException iae) {
            value = "<error>";
            updateBorder(true);
        }
        _value.setText(String.valueOf(value));
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
        }

        // submit an attribute changed event with the new value
        if (value != null) {
            AttributeChangedEvent ace = new AttributeChangedEvent(
                _object.getOid(), _field.getName(), value);
            _ctx.getDObjectManager().postEvent(ace);
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
    protected JTextField _value;
}
