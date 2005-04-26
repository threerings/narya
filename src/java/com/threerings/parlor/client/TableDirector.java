//
// $Id$
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

package com.threerings.parlor.client;

import java.util.ArrayList;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;

import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.Log;
import com.threerings.parlor.data.Table;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.data.TableLobbyObject;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.util.ParlorContext;

/**
 * As tables are created and managed within the scope of a place (a
 * lobby), we want to fold the table management functionality into the
 * standard hierarchy of place controllers that deal with place-related
 * functionality on the client. Thus, instead of forcing places that
 * expect to have tables to extend a <code>TableLobbyController</code> or
 * something similar, we instead provide the table director which can be
 * instantiated by the place controller (or specific table related views)
 * to handle the table matchmaking services.
 *
 * <p> Entites that do so, will need to implement the {@link
 * TableObserver} interface so that the table director can notify them
 * when table related things happen.
 *
 * <p> The table services expect that the place object being used as a
 * lobby in which the table matchmaking takes place implements the {@link
 * TableLobbyObject} interface.
 */
public class TableDirector extends BasicDirector
    implements SetListener, ParlorService.TableListener
{
    /**
     * Creates a new table director to manage tables with the specified
     * observer which will receive callbacks when interesting table
     * related things happen.
     *
     * @param ctx the parlor context in use by the client.
     * @param tableField the field name of the distributed set that
     * contains the tables we will be managing.
     * @param observer the entity that will receive callbacks when things
     * happen to the tables.
     */
    public TableDirector (
        ParlorContext ctx, String tableField, TableObserver observer)
    {
        super(ctx);

        // keep track of this stuff
        _ctx = ctx;
        _tableField = tableField;
        _observer = observer;
    }

    /**
     * This must be called by the entity that uses the table director when
     * the using entity prepares to enter and display a place. It is
     * assumed that the client is already subscribed to the provided place
     * object.
     */
    public void willEnterPlace (PlaceObject place)
    {
        // add ourselves as a listener to the place object
        place.addListener(this);

        // and remember this for later
        _lobby = place;
    }

    /**
     * This must be called by the entity that uses the table director when
     * the using entity has left and is done displaying a place.
     */
    public void didLeavePlace (PlaceObject place)
    {
        // remove our listenership
        place.removeListener(this);

        // clear out our lobby reference
        _lobby = null;
    }

    /**
     * Requests that the specified observer be added to the list of
     * observers that are notified when this client sits down at or stands
     * up from a table.
     */
    public void addSeatednessObserver (SeatednessObserver observer)
    {
        _seatedObservers.add(observer);
    }

    /**
     * Requests that the specified observer be removed from to the list of
     * observers that are notified when this client sits down at or stands
     * up from a table.
     */
    public void removeSeatednessObserver (SeatednessObserver observer)
    {
        _seatedObservers.remove(observer);
    }

    /**
     * Returns true if this client is currently seated at a table, false
     * if they are not.
     */
    public boolean isSeated ()
    {
        return (_ourTable != null);
    }

    /**
     * Sends a request to create a table with the specified game
     * configuration. This user will become the owner of this table and
     * will be added to the first position in the table. The response will
     * be communicated via the {@link TableObserver} interface.
     */
    public void createTable (TableConfig tableConfig, GameConfig config)
    {
        // if we're already in a table, refuse the request
        if (_ourTable != null) {
            Log.warning("Ignoring request to create table as we're " +
                        "already in a table [table=" + _ourTable + "].");
            return;
        }

        // make sure we're currently in a place
        if (_lobby == null) {
            Log.warning("Requested to create a table but we're not " +
                        "currently in a place [config=" + config + "].");
            return;
        }

        // go ahead and issue the create request
        _pservice.createTable(_ctx.getClient(), _lobby.getOid(), tableConfig,
            config, this);
    }

    /**
     * Sends a request to join the specified table at the specified
     * position. The response will be communicated via the {@link
     * TableObserver} interface.
     */
    public void joinTable (int tableId, int position)
    {
        // if we're already in a table, refuse the request
        if (_ourTable != null) {
            Log.warning("Ignoring request to join table as we're " +
                        "already in a table [table=" + _ourTable + "].");
            return;
        }

        // make sure we're currently in a place
        if (_lobby == null) {
            Log.warning("Requested to join a table but we're not " +
                        "currently in a place [tableId=" + tableId + "].");
            return;
        }

        // issue the join request
        _pservice.joinTable(_ctx.getClient(), _lobby.getOid(), tableId,
                            position, this);
    }

    /**
     * Sends a request to leave the specified table at which we are
     * presumably seated. The response will be communicated via the {@link
     * TableObserver} interface.
     */
    public void leaveTable (int tableId)
    {
        // make sure we're currently in a place
        if (_lobby == null) {
            Log.warning("Requested to leave a table but we're not " +
                        "currently in a place [tableId=" + tableId + "].");
            return;
        }

        // issue the leave request
        _pservice.leaveTable(_ctx.getClient(), _lobby.getOid(), tableId, this);
    }

    // documentation inherited
    public void clientDidLogoff (Client client)
    {
        super.clientDidLogoff(client);
        _pservice = null;
        _lobby = null;
        _ourTable = null;
    }

    // documentation inherited
    protected void fetchServices (Client client)
    {
        // get a handle on our parlor services
        _pservice = (ParlorService)client.requireService(ParlorService.class);
    }

    // documentation inherited
    public void entryAdded (EntryAddedEvent event)
    {
        if (event.getName().equals(_tableField)) {
            Table table = (Table)event.getEntry();

            // check to see if we just joined a table
            checkSeatedness(table);

            // now let the observer know what's up
            _observer.tableAdded(table);
        }
    }

    // documentation inherited
    public void entryUpdated (EntryUpdatedEvent event)
    {
        if (event.getName().equals(_tableField)) {
            Table table = (Table)event.getEntry();

            // check to see if we just joined or left a table
            checkSeatedness(table);

            // now let the observer know what's up
            _observer.tableUpdated(table);
        }
    }

    // documentation inherited
    public void entryRemoved (EntryRemovedEvent event)
    {
        if (event.getName().equals(_tableField)) {
            Integer tableId = (Integer)event.getKey();

            // check to see if our table just disappeared
            if (_ourTable != null && tableId.equals(_ourTable.tableId)) {
                _ourTable = null;
                notifySeatedness(false);
            }

            // now let the observer know what's up
            _observer.tableRemoved(tableId.intValue());
        }
    }

    // documentation inherited from interface
    public void tableCreated (int tableId)
    {
        // nothing much to do here
        Log.info("Table creation succeeded [tableId=" + tableId + "].");
    }

    // documentation inherited from interface
    public void requestFailed (String reason)
    {
        Log.warning("Table creation failed [reason=" + reason + "].");
    }

    /**
     * Checks to see if we're a member of this table and notes it as our
     * table, if so.
     */
    protected void checkSeatedness (Table table)
    {
        Table oldTable = _ourTable;

        // if this is the same table as our table, clear out our table
        // reference and allow it to be added back if we are still in the
        // table
        if (table.equals(_ourTable)) {
            _ourTable = null;
        }

        // look for our username in the occupants array
        BodyObject self = (BodyObject)_ctx.getClient().getClientObject();
        for (int i = 0; i < table.occupants.length; i++) {
            if (self.username.equals(table.occupants[i])) {
                _ourTable = table;
                break;
            }
        }

        // if nothing changed, bail now
        if (oldTable == _ourTable ||
            (oldTable != null && oldTable.equals(_ourTable))) {
            return;
        }

        // otherwise notify the observers
        notifySeatedness(_ourTable != null);
    }

    /**
     * Notifies the seatedness observers of a seatedness change.
     */
    protected void notifySeatedness (boolean isSeated)
    {
        int slength = _seatedObservers.size();
        for (int i = 0; i < slength; i++) {
            SeatednessObserver observer = (SeatednessObserver)
                _seatedObservers.get(i);
            try {
                observer.seatednessDidChange(isSeated);
            } catch (Exception e) {
                Log.warning("Observer choked in seatednessDidChange() " +
                            "[observer=" + observer + "].");
                Log.logStackTrace(e);
            }
        }
    }

    /** A context by which we can access necessary client services. */
    protected ParlorContext _ctx;

    /** Parlor server-side services. */
    protected ParlorService _pservice;

    /** The place object in which we're currently managing tables. */
    protected PlaceObject _lobby;

    /** The field name of the distributed set that contains our tables. */
    protected String _tableField;

    /** The entity that we talk to when table stuff happens. */
    protected TableObserver _observer;

    /** The table of which we are a member if any. */
    protected Table _ourTable;

    /** An array of entities that want to hear about when we stand up or
     * sit down. */
    protected ArrayList _seatedObservers = new ArrayList();
}
