//
// $Id: MiCasaClient.java,v 1.11 2002/03/28 22:32:31 mdb Exp $

package com.threerings.micasa.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.threerings.util.MessageManager;

import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DObjectManager;

import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.client.OccupantManager;
import com.threerings.crowd.client.PlaceView;

import com.threerings.parlor.client.ParlorDirector;

import com.threerings.micasa.Log;
import com.threerings.micasa.util.MiCasaContext;

/**
 * The MiCasa client takes care of instantiating all of the proper
 * managers and loading up all of the necessary configuration and getting
 * the client bootstrapped. It can be extended by games that require an
 * extended context implementation.
 */
public class MiCasaClient
    implements Client.Invoker
{
    /**
     * Initializes a new client and provides it with a frame in which to
     * display everything.
     */
    public void init (MiCasaFrame frame)
        throws IOException
    {
        // create our context
        _ctx = createContextImpl();

        // create the directors/managers/etc. provided by the context
        createContextServices();

        // for test purposes, hardcode the server info
        _client.setServer("bering", 4007);

        // keep this for later
        _frame = frame;

        // log off when they close the window
        _frame.addWindowListener(new WindowAdapter() {
            public void windowClosing (WindowEvent evt) {
                // if we're logged on, log off
                if (_client.loggedOn()) {
                    _client.logoff(true);
                }
            }
        });

        // create our client controller and stick it in the frame
        _frame.setController(new ClientController(_ctx, _frame));
    }

    /**
     * Returns a reference to the context in effect for this client. This
     * reference is valid for the lifetime of the application.
     */
    public MiCasaContext getContext ()
    {
        return _ctx;
    }

    /**
     * Creates the {@link MiCasaContext} implementation that will be
     * passed around to all of the client code. Derived classes may wish
     * to override this and create some extended context implementation.
     */
    protected MiCasaContext createContextImpl ()
    {
        return new MiCasaContextImpl();
    }

    /**
     * Creates and initializes the various services that are provided by
     * the context. Derived classes that provide an extended context
     * should override this method and create their own extended
     * services. They should be sure to call
     * <code>super.createContextServices</code>.
     */
    protected void createContextServices ()
        throws IOException
    {
        // create the handles on our various services
        _client = new Client(null, this);

        // create our managers and directors
        _locdir = new LocationDirector(_ctx);
        _occmgr = new OccupantManager(_ctx);
        _pardtr = new ParlorDirector(_ctx);
        _msgmgr = new MessageManager(MESSAGE_MANAGER_PREFIX);
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
        /**
         * Apparently the default constructor has default access, rather
         * than protected access, even though this class is declared to be
         * protected. Why, I don't know, but we need to be able to extend
         * this class elsewhere, so we need this.
         */
        protected MiCasaContextImpl ()
        {
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

        public MiCasaFrame getFrame ()
        {
            return _frame;
        }

        public MessageManager getMessageManager ()
        {
            return _msgmgr;
        }
    }

    protected MiCasaContext _ctx;
    protected MiCasaFrame _frame;

    protected Client _client;
    protected LocationDirector _locdir;
    protected OccupantManager _occmgr;
    protected ParlorDirector _pardtr;
    protected MessageManager _msgmgr;

    /** The prefix prepended to localization bundle names before looking
     * them up in the classpath. */
    protected static final String MESSAGE_MANAGER_PREFIX = "rsrc.messages";
}
