//
// $Id: ParlorService.java,v 1.16 2004/08/27 02:20:12 mdb Exp $
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

import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

import com.threerings.parlor.game.GameConfig;

/**
 * Provides an interface to the various parlor invocation services.
 * Presently these services are limited to the various matchmaking
 * mechanisms. It is unlikely that client code will want to make direct
 * use of this class, instead they would make use of the programmatic
 * interface provided by the {@link ParlorDirector}.
 */
public interface ParlorService extends InvocationService
{
    /**
     * Used to communicate responses to {@link #invite} requests.
     */
    public static interface InviteListener extends InvocationListener
    {
        /**
         * Called in response to a successful {@link #invite} request.
         */
        public void inviteReceived (int inviteId);
    }

    /**
     * You probably don't want to call this directly, but want to generate
     * your invitation request via {@link ParlorDirector#invite}. Requests
     * that an invitation be delivered to the named user, requesting that
     * they join the inviting user in a game, the details of which are
     * specified in the supplied game config object.
     *
     * @param client a connected, operational client instance.
     * @param invitee the username of the user to be invited.
     * @param config a game config object detailing the type and
     * configuration of the game to be created.
     * @param listener will receive and process the response.
     */
    public void invite (Client client, Name invitee, GameConfig config,
                        InviteListener listener);

    /**
     * You probably don't want to call this directly, but want to call one
     * of {@link Invitation#accept}, {@link Invitation#refuse}, or {@link
     * Invitation#counter}. Requests that an invitation response be
     * delivered with the specified parameters.
     *
     * @param client a connected, operational client instance.
     * @param inviteId the unique id previously assigned by the server to
     * this invitation.
     * @param code the response code to use in responding to the
     * invitation.
     * @param arg the argument associated with the response (a string
     * message from the player explaining why the response was refused in
     * the case of an invitation refusal or an updated game configuration
     * object in the case of a counter-invitation, or null in the case of
     * an accepted invitation).
     * @param listener will receive and process the response.
     */
    public void respond (Client client, int inviteId, int code, Object arg,
                         InvocationListener listener);

    /**
     * You probably don't want to call this directly, but want to call
     * {@link Invitation#cancel}. Requests that an outstanding
     * invitation be cancelled.
     *
     * @param client a connected, operational client instance.
     * @param inviteId the unique id previously assigned by the server to
     * this invitation.
     * @param listener will receive and process the response.
     */
    public void cancel (Client client, int inviteId,
                        InvocationListener listener);

    /**
     * Used to communicate responses to {@link #createTable}, {@link
     * #joinTable}, and {@link #leaveTable} requests.
     */
    public static interface TableListener extends InvocationListener
    {
        public void tableCreated (int tableId);
    }

    /**
     * You probably don't want to call this directly, but want to call
     * {@link TableDirector#createTable}. Requests that a new table be
     * created.
     *
     * @param client a connected, operational client instance.
     * @param lobbyOid the oid of the lobby that will contain the newly
     * created table.
     * @param config the game config for the game to be matchmade by the
     * table.
     * @param listener will receive and process the response.
     */
    public void createTable (Client client, int lobbyOid, GameConfig config,
                             TableListener listener);

    /**
     * You probably don't want to call this directly, but want to call
     * {@link TableDirector#joinTable}. Requests that the current user
     * be added to the specified table at the specified position.
     *
     * @param client a connected, operational client instance.
     * @param lobbyOid the oid of the lobby that contains the table.
     * @param tableId the unique id of the table to which this user wishes
     * to be added.
     * @param position the position at the table to which this user desires
     * to be added.
     * @param listener will receive and process the response.
     */
    public void joinTable (Client client, int lobbyOid, int tableId,
                           int position, InvocationListener listener);

    /**
     * You probably don't want to call this directly, but want to call
     * {@link TableDirector#leaveTable}. Requests that the current user be
     * removed from the specified table.
     *
     * @param client a connected, operational client instance.
     * @param lobbyOid the oid of the lobby that contains the table.
     * @param tableId the unique id of the table from which this user
     * wishes to be removed.
     * @param listener will receive and process the response.
     */
    public void leaveTable (Client client, int lobbyOid, int tableId,
                            InvocationListener listener);
}
