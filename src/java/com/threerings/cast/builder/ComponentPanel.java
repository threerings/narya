//
// $Id: ComponentPanel.java,v 1.1 2001/10/30 16:16:01 shaper Exp $

package com.threerings.cast.builder;

import java.util.*;

import javax.swing.*;
import javax.swing.border.Border;

import com.samskivert.swing.*;
import com.samskivert.util.*;

import com.threerings.cast.Log;
import com.threerings.cast.*;

/**
 * The component panel displays the available components for a
 * particular component type, allows the user to choose a set of
 * components for compositing, and makes available a {@link
 * CharacterDescriptor} suitable for passing to {@link
 * CharacterManager#getCharacter} to create a character built from the
 * chosen components.
 */
public class ComponentPanel extends JPanel
{
    /**
     * Constructs the component panel.
     */
    public ComponentPanel (CharacterManager charmgr, ComponentType type)
    {
        // save off references
        _type = type;

        // retrieve component classes and relevant components
        gatherComponentInfo(charmgr);

	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	setLayout(gl);

	// set up a border
	setBorder(BorderFactory.createEtchedBorder());

        // add the component editors to the panel
        addComponentEditors();
    }

    /**
     * Returns a {@link CharacterDescriptor} detailing the selected
     * character components.
     */
    public CharacterDescriptor getDescriptor ()
    {
        int size = _classes.size();
        int comps[] = new int[size];
        for (int ii = 0; ii < size; ii++) {
            ComponentClass cclass = (ComponentClass)_classes.get(ii);
            ComponentEditor ce = (ComponentEditor)_editors.get(ii);
            comps[cclass.clid] = ce.getSelectedComponent();
        }

        return new CharacterDescriptor(_type, comps);
    }

    /**
     * Gathers component information from the character manager for
     * later use when creating the editor components and the resulting
     * character descriptor.
     */
    protected void gatherComponentInfo (CharacterManager charmgr)
    {
        // get the list of all component classes
        CollectionUtil.addAll(_classes, charmgr.enumerateComponentClasses());

        for (int ii = 0; ii < _classes.size(); ii++) {
            // get the list of components available for this class
            int ctid = _type.ctid;
            int clid = ((ComponentClass)_classes.get(ii)).clid;
            Iterator comps = charmgr.enumerateComponentsByClass(ctid, clid);

            while (comps.hasNext()) {
                Integer cid = (Integer)comps.next();

                ArrayList clist = (ArrayList)_classinfo.get(clid);
                if (clist == null) {
                    _classinfo.put(clid, clist = new ArrayList());
                }

                clist.add(cid);
            }
        }
    }

    /**
     * Adds editor user interface elements for each component class to
     * allow the user to select the desired component.
     */
    protected void addComponentEditors ()
    {
        int size = _classes.size();
        for (int ii = 0; ii < size; ii++) {
            ComponentClass cclass = (ComponentClass)_classes.get(ii);
            List components = (List)_classinfo.get(cclass.clid);

            // create the component editor
            ComponentEditor e = new ComponentEditor(cclass, components);
            _editors.add(e);

            // add it to the panel
            add(e);
        }
    }

    /** The component type associated with the character components. */
    protected ComponentType _type;

    /** The list of all available component classes. */
    protected ArrayList _classes = new ArrayList();

    /** The list of component editors for each component class. */
    protected ArrayList _editors = new ArrayList();

    /** The hashtable of available component ids for each class. */
    protected HashIntMap _classinfo = new HashIntMap();
}
