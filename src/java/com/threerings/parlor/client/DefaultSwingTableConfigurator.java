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
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.SimpleSlider;
import com.samskivert.swing.VGroupLayout;

import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.util.ParlorContext;

import com.threerings.parlor.game.client.SwingGameConfigurator;
import com.threerings.parlor.game.data.GameConfig;

/**
 * Provides a default implementation of a TableConfigurator for
 * a Swing interface.
 */
public class DefaultSwingTableConfigurator extends TableConfigurator
{
    /**
     * Create a TableConfigurator that allows only the specified number
     * of players and lets the configuring user enable private games
     * only if the number of players is greater than 2.
     */
    public DefaultSwingTableConfigurator (int players)
    {
        this(players, (players > 2));
    }

    /**
     * Create a TableConfigurator that allows only the specified number
     * of players and lets the user configure a private table, or not.
     */
    public DefaultSwingTableConfigurator (int players, boolean allowPrivate)
    {
        this(players, players, players, allowPrivate);
    }

    /**
     * Create a TableConfigurator that allows for the specified configuration
     * parameters.
     */
    public DefaultSwingTableConfigurator (int minPlayers,
            int desiredPlayers, int maxPlayers, boolean allowPrivate)
    {
        _config.minimumPlayerCount = minPlayers;

        // create a slider for players, if applicable
        if (minPlayers != maxPlayers) {
            _playerSlider = new SimpleSlider(
                "", minPlayers, maxPlayers, desiredPlayers);

        } else {
            _config.desiredPlayerCount = desiredPlayers;
        }

        // create up the checkbox for private games, if applicable
        if (allowPrivate) {
            _privateCheck = new JCheckBox();
        }
    }

    // documentation inherited
    protected void createConfigInterface ()
    {
        super.createConfigInterface();

        SwingGameConfigurator gconf = (SwingGameConfigurator) _gameConfigurator;

        if (_playerSlider != null) {
            // TODO: proper translation
            gconf.addControl(new JLabel("Players:"), _playerSlider);
        }

        if (_privateCheck != null) {
            // TODO: proper translation
            gconf.addControl(new JLabel("Private:"), _privateCheck);
        }
    }

    // documentation inherited
    public boolean isEmpty ()
    {
        return (_playerSlider == null) && (_privateCheck == null);
    }

    // documentation inherited
    protected void flushTableConfig()
    {
        super.flushTableConfig();

        if (_playerSlider != null) {
            _config.desiredPlayerCount = _playerSlider.getValue();
        }
        if (_privateCheck != null) {
            _config.privateTable = _privateCheck.isSelected();
        }
    }

    /** A slider for configuring the number of players at the table. */
    protected SimpleSlider _playerSlider;

    /** A checkbox to allow the table creator to specify if the table is
     * private. */
    protected JCheckBox _privateCheck;
}
