//
// $Id: ConfigEditorPanel.java,v 1.6 2002/09/25 03:01:14 mdb Exp $

package com.threerings.admin.client;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.AncestorEvent;

import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.event.AncestorAdapter;
import com.samskivert.util.StringUtil;

import com.threerings.media.SafeScrollPane;
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
        _ctx = ctx;

        setLayout(new VGroupLayout(VGroupLayout.STRETCH, VGroupLayout.STRETCH,
                                   5, VGroupLayout.CENTER));

        // create our objects tabbed pane
        add(_oeditors = new JTabbedPane());

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
            SafeScrollPane scrolly = (SafeScrollPane)_oeditors.getComponent(ii);
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
            SafeScrollPane scrolly = new SafeScrollPane(panel);
            _oeditors.addTab(keys[ii], scrolly);
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
}
