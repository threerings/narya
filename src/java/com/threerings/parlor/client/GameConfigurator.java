//
// $Id: GameConfigurator.java,v 1.1 2002/07/25 23:20:22 mdb Exp $

package com.threerings.parlor.client;

import javax.swing.JPanel;
import com.samskivert.swing.VGroupLayout;

import com.threerings.parlor.game.GameConfig;
import com.threerings.parlor.util.ParlorContext;

/**
 * Provides the base from which interfaces can be built to configure games
 * prior to starting them. Derived classes would extend the base
 * configurator adding interface elements and wiring them up properly to
 * allow the user to configure an instance of their game.
 *
 * <p> Clients that use the game configurator will want to instantiate one
 * based on the class returned from the {@link GameConfig} and then
 * initialize it with a call to {@link #init}.
 */
public class GameConfigurator extends JPanel
{
    /**
     * Initializes this game configurator, creates its user interface
     * elements and prepares it for display.
     */
    public void init (ParlorContext ctx)
    {
        // save this for later
        _ctx = ctx;

        // set up our layout manager
        VGroupLayout layout = new VGroupLayout(VGroupLayout.NONE);
        layout.setOffAxisPolicy(VGroupLayout.STRETCH);
        setLayout(layout);

        // create our interface elements
        createConfigInterface();
    }

    /**
     * The default implementation creates a label indicating that no game
     * specific configurations are available.
     */
    protected void createConfigInterface ()
    {
    }

    /**
     * Provides this configurator with its configuration. It should set up
     * all of its user interface elements to reflect the configuration.
     */
    public void setGameConfig (GameConfig config)
    {
        _config = config;
        // set up the user interface
        gotGameConfig();
    }

    /**
     * Derived classes will likely want to override this method and
     * configure their user interface elements accordingly.
     */
    protected void gotGameConfig ()
    {
    }

    /**
     * Obtains a configured game configuration.
     */
    public GameConfig getGameConfig ()
    {
        // flush our changes to the config object
        flushGameConfig();
        return _config;
    }

    /**
     * Derived classes will want to override this method, flushing values
     * from the user interface to the game config object so that it is
     * properly configured prior to being returned to the {@link
     * #getGameConfig} caller.
     */
    protected void flushGameConfig ()
    {
    }

    /** Provides access to client services. */
    protected ParlorContext _ctx;

    /** Our game configuration. */
    protected GameConfig _config;
}
