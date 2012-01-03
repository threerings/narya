//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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
