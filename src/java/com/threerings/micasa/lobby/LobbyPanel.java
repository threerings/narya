//
// $Id: LobbyPanel.java,v 1.1 2001/10/03 23:24:09 mdb Exp $

package com.threerings.micasa.client;

import javax.swing.*;
import com.samskivert.swing.*;

/**
 * The main panel is the top level component in the UI. It encloses the
 * primary user interface display and the sidebar displays. The sidebar
 * contains a navigation display, a management display (where various
 * management panels can be added), and the chat interface.
 */
public class MainPanel extends JPanel
{
    public MainPanel ()
    {
        GroupLayout gl = new HGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        gl.setJustification(GroupLayout.RIGHT);
        gl.setGap(0);
        setLayout(gl);

        // create our sidebar panel
        gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        _sidePanel = new JPanel(gl);
  	_sidePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // stick some stuff in our sidebar for now
        JLabel label = new JLabel("Sidebar views");
        _sidePanel.add(label, GroupLayout.FIXED);
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

    /**
     * Sets the navigation view. This is the small map-like view in the
     * upper right corner of the display.
     */
    public void setNavigation (JPanel view)
    {
        // remove the old view
        if (_navView != null) {
            _sidePanel.remove(_navView);
        }

        // add the new one
        _navView = view;
        _sidePanel.add(_navView);
        _sidePanel.validate();
    }

    /** The sidebar panel. */
    protected JPanel _sidePanel;

    /** The primary view. */
    protected JPanel _primView;

    /** The navigation view. */
    protected JPanel _navView;
}
