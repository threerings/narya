//
// $Id: AI.java,v 1.1 2004/10/22 19:27:54 ray Exp $

package com.threerings.parlor.game;

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
