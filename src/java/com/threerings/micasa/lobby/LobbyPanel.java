//
// $Id: LobbyPanel.java,v 1.2 2001/10/04 23:41:44 mdb Exp $

package com.threerings.micasa.client;

import javax.swing.*;
import com.samskivert.swing.*;
import com.threerings.micasa.util.MiCasaContext;

/**
 * The lobby panel is used to display the interface for the lobbies. It
 * contains a lobby selection mechanism, a chat interface and a user
 * interface for whatever matchmaking mechanism is appropriate for a
 * particular lobby.
 */
public class LobbyPanel extends JPanel
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

        // create our sidebar panel
        gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        _sidePanel = new JPanel(gl);

        // the sidebar contains a lobby selector...
        JLabel label = new JLabel("Select a lobby...");
        _sidePanel.add(label, GroupLayout.FIXED);
        LobbySelector selector = new LobbySelector(ctx);
        _sidePanel.add(selector);

        // ...a lobby info display...

        // and an occupants list
        label = new JLabel("Occupants");
        _sidePanel.add(label, GroupLayout.FIXED);
        OccupantList occupants = new OccupantList(ctx);
        _sidePanel.add(occupants);

        // add our sidebar panel into the mix
        add(_sidePanel, GroupLayout.FIXED);
    }

    /**
     * Sets the primary view. This is the large view that displays the
     * primary activity going on in the client (the iso view when they're
     * walking around, the puzzle view when they're puzzling, etc.).
     */
    public void setPrimary (JPanel view)
    {
        // ignore requests to set the same view. it's simplest to deal
        // with this here
        if (view == _primView) {
            return;
        }

        // remove the previous primary view
        if (_primView != null) {
            remove(_primView);
        }

        // add the new view
        _primView = view;
        add(_primView, 0);
        validate();
    }

    /** The sidebar panel. */
    protected JPanel _sidePanel;

    /** The primary view. */
    protected JPanel _primView;
}
