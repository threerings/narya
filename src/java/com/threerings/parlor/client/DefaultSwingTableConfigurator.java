//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.client;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.samskivert.swing.SimpleSlider;
import com.samskivert.swing.VGroupLayout;

import com.threerings.parlor.data.TableConfigurator;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.util.ParlorContext;


/**
 * Provides a default implementation of a TableConfigurator for
 * a Swing interface.
 */
public class DefaultSwingTableConfigurator extends JPanel
    implements TableConfigurator
{
    /**
     * Create a TableConfigurator that allows only the specified number
     * of players and lets the configuring user enable private games
     * only if the number of players is greater than 2.
     */
    public DefaultSwingTableConfigurator (ParlorContext ctx, int players)
    {
        this(ctx, players, (players > 2));
    }

    /**
     * Create a TableConfigurator that allows only the specified number
     * of players and lets the user configure a private table, or not.
     */
    public DefaultSwingTableConfigurator (ParlorContext ctx, int players,
            boolean allowPrivate)
    {
        this(ctx, players, players, players, allowPrivate);
    }

    /**
     * Create a TableConfigurator that allows for the specified configuration
     * parameters.
     */
    public DefaultSwingTableConfigurator (ParlorContext ctx, int minPlayers,
            int desiredPlayers, int maxPlayers, boolean allowPrivate)
    {
        super(new VGroupLayout()); // TODO: layout improvement?

        // TODO: translations
        _playerSlider = new SimpleSlider(
            "Seats:", minPlayers, maxPlayers, desiredPlayers);
        _privateCheck = new JCheckBox("Private?:");

        // figure out what to actually show
        if (minPlayers != maxPlayers) {
            add(_playerSlider);
        }
        if (allowPrivate) {
            add(_privateCheck);
        }
    }

    // documentation inherited from interface TableConfigurator
    public boolean isEmpty ()
    {
        return (getComponentCount() == 0);
    }

    // documentation inherited from interface TableConfigurator
    public TableConfig getTableConfig ()
    {
        TableConfig tconfig = new TableConfig();
        tconfig.desiredPlayerCount = _playerSlider.getValue();
        tconfig.privateTable = _privateCheck.isSelected();

        return tconfig;
    }

    /** A slider for configuring the number of players at the table. */
    protected SimpleSlider _playerSlider;

    /** A checkbox to allow the table creator to specify if the table is
     * private. */
    protected JCheckBox _privateCheck;
}
