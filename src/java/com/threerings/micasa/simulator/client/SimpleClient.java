//
// $Id: SimpleClient.java,v 1.9 2003/11/24 17:51:56 mdb Exp $

package com.threerings.micasa.simulator.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.samskivert.util.Config;
import com.threerings.util.MessageManager;

import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DObjectManager;

import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.client.OccupantDirector;
import com.threerings.crowd.client.PlaceView;

import com.threerings.parlor.client.ParlorDirector;
import com.threerings.parlor.util.ParlorContext;

import com.threerings.micasa.Log;
import com.threerings.micasa.client.MiCasaFrame;
import com.threerings.micasa.simulator.data.SimulatorInfo;
import com.threerings.micasa.util.MiCasaContext;

public class SimpleClient
    implements Client.Invoker, SimulatorClient
{
    public SimpleClient (SimulatorFrame frame)
        throws IOException
    {
        // create our context
        _ctx = new MiCasaContextImpl();

        // create the handles on our various services
        _client = new Client(null, this);

        // create our managers and directors
        _msgmgr = new MessageManager("rsrc");
        _locdir = new LocationDirector(_ctx);
        _occdir = new OccupantDirector(_ctx);
        _pardtr = new ParlorDirector(_ctx);

        // keep this for later
        _frame = frame;

        // log off when they close the window
        _frame.getFrame().addWindowListener(new WindowAdapter() {
            public void windowClosing (WindowEvent evt) {
                // if we're logged on, log off
                if (_client.isLoggedOn()) {
                    _client.logoff(true);
                }
            }
        });
    }

    /**
     * Returns a reference to the context in effect for this client. This
     * reference is valid for the lifetime of the application.
     */
    public ParlorContext getParlorContext ()
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
    protected class MiCasaContextImpl implements MiCasaContext
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

        public OccupantDirector getOccupantDirector ()
        {
            return _occdir;
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

        public void clearPlaceView (PlaceView view)
        {
            // we'll just let the next view replace the old one
        }

        public MiCasaFrame getFrame ()
        {
            return (MiCasaFrame)_frame;
        }

        public MessageManager getMessageManager ()
        {
            return _msgmgr;
        }
    }

    protected MiCasaContext _ctx;
    protected SimulatorFrame _frame;
    protected MessageManager _msgmgr;

    protected Config _config = new Config("micasa");
    protected Client _client;
    protected LocationDirector _locdir;
    protected OccupantDirector _occdir;
    protected ParlorDirector _pardtr;
}

