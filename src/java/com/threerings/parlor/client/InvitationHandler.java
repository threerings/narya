//
// $Id: InvitationHandler.java,v 1.5 2004/08/27 02:20:12 mdb Exp $
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

/**
 * A client entity that wishes to handle invitations received by other
 * clients should implement this interface and register itself with the
 * parlor director. It will subsequently be notified of any incoming
 * invitations. It is also responsible for handling cancelled invitations.
 */
public interface InvitationHandler
{
    /**
     * Called when an invitation is received from another player.
     *
     * @param invite the received invitation.
     */
    public void invitationReceived (Invitation invite);

    /**
     * Called when an invitation is cancelled by the inviting player.
     *
     * @param invite the cancelled invitation.
     */
    public void invitationCancelled (Invitation invite);
}
