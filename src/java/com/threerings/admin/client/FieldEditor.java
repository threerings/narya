//
// $Id: FieldEditor.java,v 1.1 2002/06/07 06:22:24 mdb Exp $

package com.threerings.admin.client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.samskivert.swing.HGroupLayout;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.util.PresentsContext;

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
        String value = "";
        try {
            value = String.valueOf(_field.get(_object));
        } catch (IllegalAccessException iae) {
            value = "<error>";
        }
        add(_value = new JTextField(value));
        _value.addActionListener(this);
    }

    // documentation inherited from interface
    public void attributeChanged (AttributeChangedEvent event)
    {
        _value.setText(String.valueOf(event.getValue()));
        updateBorder(false);
    }

    // documentation inherited from interface
    public void actionPerformed (ActionEvent event)
    {
        if (_field.getType().equals(Integer.class) ||
            _field.getType().equals(Integer.TYPE)) {
            try {
                Integer value = new Integer(_value.getText());
                AttributeChangedEvent ace = new AttributeChangedEvent(
                    _object.getOid(), _field.getName(), value);
                _ctx.getDObjectManager().postEvent(ace);
            } catch (NumberFormatException nfe) {
                updateBorder(true);
            }
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
