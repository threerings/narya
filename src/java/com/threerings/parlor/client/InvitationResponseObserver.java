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

import com.threerings.parlor.game.data.GameConfig;

/**
 * A client entity that wishes to generate invitations for games must
 * implement this interface. An invitation can be accepted, refused or
 * countered. A countered invitation is one where the game configuration
 * is adjusted by the invited player and proposed back to the inviting
 * player.
 */
public interface InvitationResponseObserver
{
    /**
     * Called if the invitation was accepted.
     *
     * @param invite the invitation for which we received a response.
     */
    public void invitationAccepted (Invitation invite);

    /**
     * Called if the invitation was refused.
     *
     * @param invite the invitation for which we received a response.
     * @param message a message provided by the invited user explaining
     * the reason for their refusal, or the empty string if no message was
     * provided.
     */
    public void invitationRefused (Invitation invite, String message);

    /**
     * Called if the invitation was countered with an alternate game
     * configuration.
     *
     * @param invite the invitation for which we received a response.
     * @param config the game configuration proposed by the invited
     * player.
     */
    public void invitationCountered (Invitation invite, GameConfig config);
}
