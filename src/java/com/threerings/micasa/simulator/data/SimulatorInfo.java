//
// $Id: SimulatorInfo.java,v 1.1 2001/12/19 09:32:02 shaper Exp $

package com.threerings.micasa.simulator.data;

public class SimulatorInfo
{
    /** The game config classname. */
    public String gameConfigClass;

    /** The simulant classname. */
    public String simClass;

    /** The number of players in the game. */
    public int playerCount;

    public String toString ()
    {
        return "[gameConfigClass=" + gameConfigClass +
            ", simClass=" + simClass + ", playerCount=" + playerCount + "]";
    }
}
