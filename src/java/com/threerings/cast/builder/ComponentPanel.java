//
// $Id: ComponentPanel.java,v 1.8 2004/02/25 14:39:34 mdb Exp $

package com.threerings.cast.builder;

import java.util.List;

import javax.swing.*;

import com.samskivert.swing.*;

import com.threerings.cast.Log;
import com.threerings.cast.*;

/**
 * The component panel displays the available components for all
 * component classes and allows the user to choose a set of components
 * for compositing into a character image.
 */
public class ComponentPanel extends JPanel
{
    /**
     * Constructs the component panel.
     */
    public ComponentPanel (BuilderModel model, String cprefix)
    {
	setLayout(new VGroupLayout(GroupLayout.STRETCH));
	// set up a border
	setBorder(BorderFactory.createEtchedBorder());
        // add the component editors to the panel
        addClassEditors(model, cprefix);
    }

    /**
     * Adds editor user interface elements for each component class to
     * allow the user to select the desired component.
     */
    protected void addClassEditors (BuilderModel model, String cprefix)
    {
        List classes = model.getComponentClasses();
        int size = classes.size();
        for (int ii = 0; ii < size; ii++) {
            ComponentClass cclass = (ComponentClass)classes.get(ii);
            if (!cclass.name.startsWith(cprefix)) {
                continue;
            }
            List ccomps = model.getComponents(cclass);
            if (ccomps.size() > 0) {
                add(new ClassEditor(model, cclass, ccomps));
            } else {
                Log.info("Not creating editor for empty class " +
                         "[class=" + cclass + "].");
            }
        }
    }
}
