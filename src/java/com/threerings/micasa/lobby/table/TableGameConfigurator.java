//
// $Id: TableGameConfigurator.java,v 1.2 2004/02/25 14:43:37 mdb Exp $

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
