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

import com.threerings.parlor.util.ParlorContext;

import com.threerings.parlor.data.TableConfig;

import com.threerings.parlor.game.client.GameConfigurator;

/**
 * This should be implemented some user-interface element that allows
 * the user to configure whichever TableConfig options are relevant.
 */
public abstract class TableConfigurator
{
    /**
     * Create a TableConfigurator.
     */
    public TableConfigurator ()
    {
        _config = createTableConfig();
    }

    /**
     * Initialize the TableConfigurator.
     *
     * At the time this is called, the GameConfigurator should have
     * already been initialized.
     */
    public void init (ParlorContext ctx, GameConfigurator gameConfigurator)
    {
        _ctx = ctx;
        _gameConfigurator = gameConfigurator;
        createConfigInterface();
    }

    /**
     * Create the table config object that will be used.
     */
    protected TableConfig createTableConfig ()
    {
        return new TableConfig();
    }

    /**
     * Create the config interface.
     */
    protected void createConfigInterface ()
    {
        // nothing by default
    }

    /**
     * If true, the TableConfigurator is empty, which doesn't mean that 
     * it will not return a TableConfig object (for it must), but rather
     * that there are no user-editable options being presented in the
     * config interface.
     */
    public abstract boolean isEmpty ();

    /**
     * Return the fully configured table config according to the currently
     * configured user interface elements.
     */
    public TableConfig getTableConfig ()
    {
        flushTableConfig();
        return _config;
    }

    /**
     * Derived classes will want to override this method, flushing
     * values from the user interface to the table config object.
     */
    protected void flushTableConfig ()
    {
        // nothing by default
    }

    /** Provides access to client services. */
    protected ParlorContext _ctx;

    /** The config we're configurating. */
    protected TableConfig  _config;

    /** The game configurator, which we may wish to reference. */
    protected GameConfigurator _gameConfigurator;
}
