//
// $Id: SoundCodes.java,v 1.1 2003/11/26 01:43:31 mdb Exp $

package com.threerings.media.sound;

import com.threerings.media.sound.SoundManager.SoundType;

/**
 * A basic set of sound types.
 */
public interface SoundCodes
{
    /**
     * Alert sounds are the type of sounds a player would hear when
     * getting a puzzle challenge.
     */
    public static final SoundType ALERT = new SoundType("alert");

    /**
     * Feedback sounds are the type of sounds a player would here when
     * clicking on buttons or performing an action.
     */
    public static final SoundType FEEDBACK = new SoundType("feedback");

    /**
     * Ambient sounds are birds chirping, waves lapping, boats creaking.
     */
    public static final SoundType AMBIENT = new SoundType("ambient");

    /**
     * Game alert sounds are used to indicate that it's a player's turn.
     */
    public static final SoundType GAME_ALERT = new SoundType("game_alert");

    /**
     * General game sound effects.
     */
    public static final SoundType GAME_FX =new SoundType("game_fx");
}
