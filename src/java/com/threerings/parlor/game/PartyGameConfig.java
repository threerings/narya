//
// $Id: PartyGameConfig.java,v 1.1 2003/02/12 05:34:53 mdb Exp $

package com.threerings.parlor.game;

import com.threerings.parlor.data.TableConfig;

/**
 * Provides additional information for party games.
 */
public interface PartyGameConfig extends TableConfig
{
    /**
     * Returns true if this party game is being played in party game mode,
     * false if it is not.
     */
    public boolean isPartyGame ();

    /**
     * Configures this game config as a party game or not.
     */
    public void setPartyGame (boolean isPartyGame);

    /**
     * Returns an array of strings that describe the configuration of this
     * party game. This should eventually be rolled into a more general
     * purpose mechanism for generating descriptions of game
     * configurations as well as editors for game configurations (which
     * already exists in rudimentary form).
     */
    public String[] getPartyDescription ();
}
