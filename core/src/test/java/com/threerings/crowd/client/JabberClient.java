//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.client;

import java.io.IOException;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.util.Config;
import com.samskivert.util.RunQueue;

import com.threerings.util.MessageManager;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.SessionObserver;
import com.threerings.presents.dobj.DObjectManager;

import com.threerings.crowd.chat.client.ChatDirector;
import com.threerings.crowd.util.CrowdContext;

/**
 * The Jabber client takes care of instantiating all of the proper
 * managers and loading up all of the necessary configuration and getting
 * the client bootstrapped. It can be extended by games that require an
 * extended context implementation.
 */
public class JabberClient
    implements SessionObserver
{
    /**
     * Initializes a new client and provides it with a frame in which to
     * display everything.
     */
    public void init (JFrame frame)
        throws IOException
    {
        // create our context
        _ctx = createContextImpl();

        // create the directors/managers/etc. provided by the context
        createContextServices();

        // for test purposes, hardcode the server info
        _client.setServer("localhost", Client.DEFAULT_SERVER_PORTS);
        _client.addClientObserver(this);

        // keep this for later
        _frame = frame;

        // log off when they close the window
        _frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing (WindowEvent evt) {
                // if we're logged on, log off
                if (_client.isLoggedOn()) {
                    _client.logoff(true);
                } else {
                    // otherwise get the heck out
                    System.exit(0);
                }
            }
        });
    }

    /**
     * Sets the roomId to which we will connect upon logon.
     */
    public void setRoom (int roomId)
    {
        _roomId = roomId;
    }

    /**
     * Returns a reference to the context in effect for this client. This
     * reference is valid for the lifetime of the application.
     */
    public CrowdContext getContext ()
    {
        return _ctx;
    }

    // documentation inherited from interface SessionObserver
    public void clientWillLogon (Client client)
    {
    }

    // documentation inherited from interface SessionObserver
    public void clientDidLogon (Client client)
    {
        // enter the one and only chat room; giant hack warning: we know
        // that the place we're headed to is ID 2 because there's only one
        // place on the whole server; a normal client would either issue
        // an invocation service request at this point or look at
        // something in the BootstrapData to figure out what to do
        _ctx.getLocationDirector().moveTo(_roomId);
    }

    // documentation inherited from interface SessionObserver
    public void clientObjectDidChange (Client client)
    {
        // nada
    }

    // documentation inherited from interface SessionObserver
    public void clientDidLogoff (Client client)
    {
        System.exit(0);
    }

    /**
     * Creates the {@link CrowdContext} implementation that will be
     * passed around to all of the client code. Derived classes may wish
     * to override this and create some extended context implementation.
     */
    protected CrowdContext createContextImpl ()
    {
        return new JabberContextImpl();
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
        _client = new Client(null, RunQueue.AWT);

        // create our managers and directors
        _locdir = new LocationDirector(_ctx);
        _occdir = new OccupantDirector(_ctx);
        _msgmgr = new MessageManager(MESSAGE_MANAGER_PREFIX);
        _chatdir = new ChatDirector(_ctx, null);
    }

    /**
     * The context implementation. This provides access to all of the
     * objects and services that are needed by the operating client.
     */
    protected class JabberContextImpl implements CrowdContext
    {
        /**
         * Apparently the default constructor has default access, rather
         * than protected access, even though this class is declared to be
         * protected. Why, I don't know, but we need to be able to extend
         * this class elsewhere, so we need this.
         */
        protected JabberContextImpl ()
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

        public Config getConfig ()
        {
            return _config;
        }

        public LocationDirector getLocationDirector ()
        {
            return _locdir;
        }

        public OccupantDirector getOccupantDirector ()
        {
            return _occdir;
        }

        public ChatDirector getChatDirector ()
        {
            return _chatdir;
        }

        public MessageManager getMessageManager ()
        {
            return _msgmgr;
        }

        public void setPlaceView (PlaceView view)
        {
            JPanel panel = (JPanel)view;
            // remove the old panel
            _frame.getContentPane().removeAll();
            // add the new one
            _frame.getContentPane().add(panel, BorderLayout.CENTER);
            // swing doesn't properly repaint after adding/removing children
            panel.revalidate();
            _frame.repaint();
        }

        public void clearPlaceView (PlaceView view)
        {
            // we'll just let the next place view replace our old one
        }
    }

    protected CrowdContext _ctx;
    protected JFrame _frame;
    protected Config _config = new Config("jabber");

    protected Client _client;
    protected LocationDirector _locdir;
    protected OccupantDirector _occdir;
    protected ChatDirector _chatdir;
    protected MessageManager _msgmgr;
    protected int _roomId;

    /** The prefix prepended to localization bundle names before looking
     * them up in the classpath. */
    protected static final String MESSAGE_MANAGER_PREFIX = "rsrc.i18n";
}
