//
// $Id: SimpleClient.java,v 1.1 2002/01/16 02:59:08 mdb Exp $

package com.threerings.micasa.simulator.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.samskivert.util.Config;

import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DObjectManager;

import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.client.OccupantManager;
import com.threerings.crowd.client.PlaceView;

import com.threerings.parlor.client.ParlorDirector;
import com.threerings.parlor.util.ParlorContext;

import com.threerings.micasa.Log;
import com.threerings.micasa.simulator.data.SimulatorInfo;

public class SimpleClient
    implements Client.Invoker, SimulatorClient
{
    public SimpleClient (SimulatorFrame frame)
        throws IOException
    {
        // create our context
        _ctx = new ParlorContextImpl();

        // create the handles on our various services
        _config = new Config();
        _client = new Client(null, this);

        // create our managers and directors
        _locdir = new LocationDirector(_ctx);
        _occmgr = new OccupantManager(_ctx);
        _pardtr = new ParlorDirector(_ctx);

        // for test purposes, hardcode the server info
        _client.setServer("localhost", 4007);

        // keep this for later
        _frame = frame;

        // log off when they close the window
        _frame.getFrame().addWindowListener(new WindowAdapter() {
            public void windowClosing (WindowEvent evt) {
                // if we're logged on, log off
                if (_client.loggedOn()) {
                    _client.logoff(true);
                }
            }
        });
    }

    /**
     * Returns a reference to the context in effect for this client. This
     * reference is valid for the lifetime of the application.
     */
    public ParlorContext getContext ()
    {
        return _ctx;
    }

    // documentation inherited
    public void invokeLater (Runnable run)
    {
        // queue it on up on the swing thread
        SwingUtilities.invokeLater(run);
    }

    /**
     * The context implementation. This provides access to all of the
     * objects and services that are needed by the operating client.
     */
    protected class ParlorContextImpl implements ParlorContext
    {
        public Config getConfig ()
        {
            return _config;
        }

        public Client getClient ()
        {
            return _client;
        }

        public DObjectManager getDObjectManager ()
        {
            return _client.getDObjectManager();
        }

        public LocationDirector getLocationDirector ()
        {
            return _locdir;
        }

        public OccupantManager getOccupantManager ()
        {
            return _occmgr;
        }

        public ParlorDirector getParlorDirector ()
        {
            return _pardtr;
        }

        public void setPlaceView (PlaceView view)
        {
            // stick the place view into our frame
            _frame.setPanel((JPanel)view);
        }
    }

    protected ParlorContext _ctx;
    protected SimulatorFrame _frame;

    protected Config _config;
    protected Client _client;
    protected LocationDirector _locdir;
    protected OccupantManager _occmgr;
    protected ParlorDirector _pardtr;
}

