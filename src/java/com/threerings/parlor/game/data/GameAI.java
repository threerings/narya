//
// $Id$

package com.threerings.parlor.game.data;

import com.threerings.io.SimpleStreamableObject;

/**
 * Represents attributes of an AI player.
 */
public class GameAI extends SimpleStreamableObject
{
    /** The "personality" of the AI, which can be interpreted by
     * each puzzle. */
    public int personality;

    /** The skill level of the AI. */
    public int skill;

    /** A blank constructor for serialization. */
    public GameAI ()
    {
    }

    /**
     * Constructs an AI with the specified (game-interpreted) skill and
     * personality.
     */
    public GameAI (int personality, int skill)
    {
        this.personality = personality;
        this.skill = skill;
    }
}
