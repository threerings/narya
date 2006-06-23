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

package com.threerings.admin.client;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.samskivert.swing.VGroupLayout;

import com.threerings.presents.util.PresentsContext;

import com.threerings.admin.Log;

/**
 * Fetches a list of the configuration objects in use by the server and
 * displays their fields in a tree widget to be viewed and edited.
 */
public class ConfigEditorPanel extends JPanel
    implements AdminService.ConfigInfoListener
{
    /**
     * Constructs an editor panel which will use the supplied context to
     * access the distributed object services.
     */
    public ConfigEditorPanel (PresentsContext ctx)
    {
        this(ctx, null);
    }

    /**
     * Constructs an editor panel with the specified pane defaulting to
     * selected.
     */
    public ConfigEditorPanel (PresentsContext ctx, String defaultPane)
    {
        _ctx = ctx;
        _defaultPane = defaultPane;

        setLayout(new VGroupLayout(VGroupLayout.STRETCH, VGroupLayout.STRETCH,
                                   5, VGroupLayout.CENTER));

        // create our objects tabbed pane
        add(_oeditors = new JTabbedPane(JTabbedPane.LEFT));

        // add a handy label at the bottom
        add(new JLabel("Fields outline in red have been modified " +
                       "but not yet committed."), VGroupLayout.FIXED);
        add(new JLabel("Press return in a modified field to commit " +
                       "the change."), VGroupLayout.FIXED);

        // ship off a getConfigInfo request to find out what config
        // objects are available for editing
        AdminService service = (AdminService)
            _ctx.getClient().requireService(AdminService.class);
        service.getConfigInfo(_ctx.getClient(), this);
    }

    // documentation inherited
    public void removeNotify ()
    {
        super.removeNotify();

        // when we're hidden, we want to clear out our subscriptions
        int ccount = _oeditors.getComponentCount();
        for (int ii = 0; ii < ccount; ii++) {
            JScrollPane scrolly = (JScrollPane)_oeditors.getComponent(ii);
            ObjectEditorPanel opanel = (ObjectEditorPanel)
                scrolly.getViewport().getView();
            opanel.cleanup();
        }
    }

    /**
     * Called in response to our getConfigInfo server-side service
     * request.
     */
    public void gotConfigInfo (String[] keys, int[] oids)
    {
        // create object editor panels for each of the categories
        for (int ii = 0; ii < keys.length; ii++) {
            ObjectEditorPanel panel =
                new ObjectEditorPanel(_ctx, keys[ii], oids[ii]);
            JScrollPane scrolly = new JScrollPane(panel);
            _oeditors.addTab(keys[ii], scrolly);
            if (keys[ii].equals(_defaultPane)) {
                _oeditors.setSelectedComponent(scrolly);
            }
        }
    }

    // documentation inherited from interface
    public void requestFailed (String reason)
    {
        Log.warning("Failed to get config info [reason=" + reason + "].");
    }

    /** Our client context. */
    protected PresentsContext _ctx;

    /** Holds our object editors. */
    protected JTabbedPane _oeditors;

    /** Our default tab pane. */
    protected String _defaultPane;
}
