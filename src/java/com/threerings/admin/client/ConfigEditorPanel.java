//
// $Id: ConfigEditorPanel.java,v 1.3 2002/07/09 21:13:20 ray Exp $

package com.threerings.admin.client;

import javax.swing.JTabbedPane;
import javax.swing.event.AncestorEvent;

import com.samskivert.swing.event.AncestorAdapter;
import com.samskivert.util.StringUtil;

import com.threerings.media.SafeScrollPane;

import com.threerings.presents.util.PresentsContext;

import com.threerings.admin.Log;

/**
 * Fetches a list of the configuration objects in use by the server and
 * displays their fields in a tree widget to be viewed and edited.
 */
public class ConfigEditorPanel extends JTabbedPane
{
    /**
     * Constructs an editor panel which will use the supplied context to
     * access the distributed object services.
     */
    public ConfigEditorPanel (PresentsContext ctx)
    {
        _ctx = ctx;

        // when we're hidden, we want to clear out our subscriptions
        addAncestorListener(new AncestorAdapter () {
            public void ancestorRemoved (AncestorEvent event) {
                cleanup();
            }
        });

        // if we have no children, ship off a getConfigInfo request to
        // find out what config objects are available for editing
        if (getComponentCount() == 0) {
            Log.info("Sending get config info.");
            AdminService.getConfigInfo(_ctx.getClient(), this);
        }
    }

    /**
     * Called in response to our getConfigInfo server-side service
     * request.
     */
    public void handleConfigInfo (int invid, String[] keys, int[] oids)
    {
        Log.info("Got config info: " + StringUtil.toString(keys));
        // create object editor panels for each of the categories
        for (int ii = 0; ii < keys.length; ii++) {
            ObjectEditorPanel panel =
                new ObjectEditorPanel(_ctx, keys[ii], oids[ii]);
            SafeScrollPane scrolly = new SafeScrollPane(panel);
            addTab(keys[ii], scrolly);
        }
    }

    /**
     * Called when the panel is hidden; this instructs all of our object
     * editors to clear out their subscriptions.
     */
    protected void cleanup ()
    {
        int ccount = getComponentCount();
        for (int ii = 0; ii < ccount; ii++) {
            SafeScrollPane scrolly = (SafeScrollPane)getComponent(ii);
            ObjectEditorPanel opanel = (ObjectEditorPanel)
                scrolly.getViewport().getView();
            opanel.cleanup();
        }
    }

    /** Our client context. */
    protected PresentsContext _ctx;
}
