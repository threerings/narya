//
// $Id: ComponentPanel.java,v 1.9 2004/08/27 02:12:26 mdb Exp $
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
