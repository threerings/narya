//
// $Id: TableGameConfigurator.java,v 1.3 2004/08/27 02:12:51 mdb Exp $
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

package com.threerings.micasa.lobby.table;

import com.samskivert.swing.SimpleSlider;
import com.samskivert.swing.VGroupLayout;

import com.threerings.parlor.client.GameConfigurator;
import com.threerings.parlor.data.TableConfig;

/**
 * Extends the basic game configurator with elements to support the
 * configuration of standard table game configurations.
 */
public class TableGameConfigurator extends GameConfigurator
{
    // documentation inherited
    protected void createConfigInterface ()
    {
        // create a slider and associated label for configuring the number
        // of people at the table
        add(_pslide = new SimpleSlider("Seats:", 0, 10, 0), VGroupLayout.FIXED);
        // TODO: get a message bundle and translate "Seats"
    }

    // documentation inherited
    protected void gotGameConfig ()
    {
        TableConfig tconfig = (TableConfig)_config;

        // configure our slider
        _pslide.setMinimum(tconfig.getMinimumPlayers());
        _pslide.setMaximum(tconfig.getMaximumPlayers());
        _pslide.setValue(tconfig.getDesiredPlayers());

        // if the min == the max, hide the slider because it's pointless
        _pslide.setVisible(tconfig.getMinimumPlayers() !=
                           tconfig.getMaximumPlayers());
    }

    // documentation inherited
    protected void flushGameConfig ()
    {
        TableConfig tconfig = (TableConfig)_config;
        tconfig.setDesiredPlayers(_pslide.getValue());
    }

    /** Our number of players slider. */
    protected SimpleSlider _pslide;
}
