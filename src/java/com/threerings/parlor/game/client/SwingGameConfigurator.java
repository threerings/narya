//
// $Id: GameConfigurator.java 3590 2005-06-08 23:20:18Z ray $
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

package com.threerings.parlor.game.client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.samskivert.swing.VGroupLayout;

import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.util.ParlorContext;

/**
 * Provides the base from which interfaces can be built to configure games
 * prior to starting them. Derived classes would extend the base
 * configurator adding interface elements and wiring them up properly to
 * allow the user to configure an instance of their game.
 *
 * <p> Clients that use the game configurator will want to instantiate one
 * based on the class returned from the {@link GameConfig} and then
 * initialize it with a call to {@link #init}.
 */
public abstract class SwingGameConfigurator extends GameConfigurator
{
    /**
     * Get the Swing JPanel that contains the UI elements for this configurator.
     */
    public JPanel getPanel ()
    {
        return _panel;
    }

    /**
     * Add a control to the interface. This should be the standard way
     * that configurator controls are added, but note also that external
     * entities may add their own controls that are related to the game,
     * but do not directly alter the game config, so that all the controls
     * are added in a uniform manner and are well aligned.
     */
    public void addControl (JComponent label, JComponent control)
    {
        // BACKcompatible, newer code will call this method, and so
        // we need to switch the layout to gridbag instead of vgroup
        if (!(_panel.getLayout() instanceof GridBagLayout)) {
            _panel.setLayout(new GridBagLayout());
        }

        // Set up the constraints. There's really no point in saving
        // this somewhere, as they're cloned anyway with every component
        // insertion.
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0;
        lc.anchor = GridBagConstraints.NORTHWEST;
        lc.insets = _labelInsets;

        GridBagConstraints cc = new GridBagConstraints();
        cc.gridx = 1;
        cc.anchor = GridBagConstraints.NORTHWEST;
        cc.insets = _controlInsets;
        cc.gridwidth = GridBagConstraints.REMAINDER;

        // add the components
        _panel.add(label, lc);
        _panel.add(control, cc);
    }

    /** The panel on which the config options are placed. */
    protected JPanel _panel = new JPanel();

    { // initializer
        // BACKcompatible, older code (and toybox) expects the layout
        // to be a vgroup, so we default to that
        VGroupLayout layout = new VGroupLayout(VGroupLayout.NONE);
        layout.setOffAxisPolicy(VGroupLayout.STRETCH);
        _panel.setLayout(layout);
    }

    /** Insets for configuration labels. */
    protected Insets _labelInsets = new Insets(1, 0, 1, 4);

    /** Insets for configuration controls. */
    protected Insets _controlInsets = new Insets(1, 4, 1, 0);
}
