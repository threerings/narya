//
// $Id: LobbyPanel.java,v 1.8 2004/08/27 02:12:50 mdb Exp $
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

package com.threerings.micasa.lobby;

import javax.swing.*;
import com.samskivert.swing.*;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.micasa.client.*;
import com.threerings.micasa.util.MiCasaContext;

/**
 * Used to display the interface for the lobbies. It contains a lobby
 * selection mechanism, a chat interface and a user interface for whatever
 * match-making mechanism is appropriate for this particular lobby.
 */
public class LobbyPanel
    extends JPanel implements PlaceView
{
    /**
     * Constructs a new lobby panel and the associated user interface
     * elements.
     */
    public LobbyPanel (MiCasaContext ctx, LobbyConfig config)
    {
        // we want a five pixel border around everything
    	setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // create our primary layout which divides the display in two
        // horizontally
        GroupLayout gl = new HGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        gl.setJustification(GroupLayout.RIGHT);
        setLayout(gl);

        // create our main panel
        gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        _main = new JPanel(gl);

        // create our match-making view
        _main.add(config.createMatchMakingView(ctx));

        // create a chat box and stick that in as well
        _main.add(new ChatPanel(ctx));

        // now add the main panel into the mix
        add(_main);

        // create our sidebar panel
        gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        JPanel sidePanel = new JPanel(gl);

        // the sidebar contains a lobby info display...

        // ...a lobby selector...
        JLabel label = new JLabel("Select a lobby...");
        sidePanel.add(label, GroupLayout.FIXED);
        LobbySelector selector = new LobbySelector(ctx);
        sidePanel.add(selector);

        // and an occupants list
        label = new JLabel("People in lobby");
        sidePanel.add(label, GroupLayout.FIXED);
        _occupants = new OccupantList(ctx);
        sidePanel.add(_occupants);

        JButton logoff = new JButton("Logoff");
        logoff.addActionListener(Controller.DISPATCHER);
        logoff.setActionCommand("logoff");
        sidePanel.add(logoff, GroupLayout.FIXED);

        // add our sidebar panel into the mix
        add(sidePanel, GroupLayout.FIXED);
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
    }

    /** Contains the match-making view and the chatbox. */
    protected JPanel _main;

    /** Our occupant list display. */
    protected OccupantList _occupants;
}
