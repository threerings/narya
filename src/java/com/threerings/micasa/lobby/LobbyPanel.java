//
// $Id: LobbyPanel.java,v 1.3 2001/10/09 18:20:08 mdb Exp $

package com.threerings.micasa.lobby;

import javax.swing.*;
import com.samskivert.swing.*;

import com.threerings.cocktail.party.client.PlaceView;
import com.threerings.cocktail.party.data.PlaceObject;

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
    public LobbyPanel (MiCasaContext ctx)
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
        _main.add(createMatchMakingView(ctx));

        // create a chat box and stick that in as well
        JLabel label = new JLabel("Chat with other people in this lobby...");
        _main.add(label, GroupLayout.FIXED);
        _main.add(new ChatPanel(ctx));

        // create our sidebar panel
        gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        JPanel sidePanel = new JPanel(gl);

        // the sidebar contains a lobby selector...
        label = new JLabel("Select a lobby...");
        sidePanel.add(label, GroupLayout.FIXED);
        LobbySelector selector = new LobbySelector(ctx);
        sidePanel.add(selector);

        // ...a lobby info display...

        // and an occupants list
        label = new JLabel("Occupants");
        sidePanel.add(label, GroupLayout.FIXED);
        _occupants = new OccupantList(ctx);
        sidePanel.add(_occupants);

        // add our sidebar panel into the mix
        add(sidePanel, GroupLayout.FIXED);
    }

    /**
     * Derived classes override this function and create the appropriate
     * matchmaking user interface component.
     */
    protected JComponent createMatchMakingView (MiCasaContext ctx)
    {
        return new JLabel("Match-making view goes here.");
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
