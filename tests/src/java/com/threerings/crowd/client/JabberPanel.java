//
// $Id$

package com.threerings.crowd.client;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

// import com.threerings.micasa.client.ChatPanel;
// import com.threerings.micasa.client.OccupantList;

/**
 * Displays a simple chat view.
 */
public class JabberPanel extends JPanel
    implements PlaceView
{
    public JabberPanel (CrowdContext ctx)
    {
        _ctx = ctx;
        setLayout(new BorderLayout());
//         add(new ChatPanel(ctx), BorderLayout.CENTER);
//         add(new OccupantList(ctx), BorderLayout.EAST);
    }

    // documentation inherited from interface PlaceView
    public void willEnterPlace (PlaceObject plobj)
    {
    }

    // documentation inherited from interface PlaceView
    public void didLeavePlace (PlaceObject plobj)
    {
    }

    protected CrowdContext _ctx;
}
