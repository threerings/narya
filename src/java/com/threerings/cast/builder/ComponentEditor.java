//
// $Id: ComponentEditor.java,v 1.1 2001/10/30 16:16:01 shaper Exp $

package com.threerings.cast.builder;

import java.util.List;

import javax.swing.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.samskivert.swing.*;
import com.samskivert.util.StringUtil;

import com.threerings.cast.Log;
import com.threerings.cast.ComponentClass;

/**
 * The component editor displays a label and a slider that allow the
 * user to select the desired component from a list of components of
 * the same class.
 */
public class ComponentEditor extends JPanel implements ChangeListener
{
    /**
     * Constructs a component editor.
     */
    public ComponentEditor (ComponentClass cclass, List components)
    {
        _components = components;

        // Log.info("Creating editor [class=" + cclass +
        // ", components=" + StringUtil.toString(components) + "].");

        GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        setLayout(gl);

        gl = new HGroupLayout();
        gl.setJustification(GroupLayout.LEFT);
        JPanel sub = new JPanel(gl);

        sub.add(new JLabel(cclass.name + ": "));
        sub.add(_clabel = new JLabel("0"));

        add(sub);

        int max = components.size() - 1;
        _slider = new JSlider(JSlider.HORIZONTAL, 0, max, 0);
        _slider.setSnapToTicks(true);
        _slider.addChangeListener(this);
        add(_slider);
    }

    /**
     * Returns the selected component id.
     */
    public int getSelectedComponent ()
    {
        int idx = _slider.getModel().getValue();
        return ((Integer)_components.get(idx)).intValue();
    }

    // documentation inherited
    public void stateChanged (ChangeEvent e)
    {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
            _clabel.setText(Integer.toString(source.getValue()));
        }
    }

    /** The components selectable via this editor. */
    protected List _components;

    /** The slider allowing the user to select a component. */
    protected JSlider _slider;

    /** The label denoting the currently selected component index. */
    protected JLabel _clabel;
}
