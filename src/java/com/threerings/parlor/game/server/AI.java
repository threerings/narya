//
// $Id$

package com.threerings.parlor.game.server;

/**
 * Represents attributes of an AI player.
 */
public class AI
{
    /** The skill level of the AI. */
    public byte skill;

    /** The "personality" of the AI, which can be interpreted by
     * each puzzle. */
    public byte personality;

    /**
     * Construct an AI.
     */
    public AI (byte skill, byte personality)
    {
        this.skill = skill;
        this.personality = personality;
    }

    // documentation inherited
    public String toString ()
    {
        return "skill(" + skill + "), personality(" + personality + ")";
    }
}
