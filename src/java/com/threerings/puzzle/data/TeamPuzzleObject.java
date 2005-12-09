//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.puzzle.data;

import com.samskivert.util.StringUtil;

import com.threerings.util.RandomUtil;

import com.threerings.puzzle.Log;

/**
 * Does something extraordinary.
 */
public class TeamPuzzleObject extends PuzzleObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>teams</code> field. */
    public static final String TEAMS = "teams";

    /** The field name of the <code>targets</code> field. */
    public static final String TARGETS = "targets";

    /** The field name of the <code>winningTeam</code> field. */
    public static final String WINNING_TEAM = "winningTeam";
    // AUTO-GENERATED: FIELDS END

    /** The team index assigned to each player index. */
    public int[] teams;

    /** The index of the player targeted by each player. */
    public short[] targets;

    /** The team index of the winning teams, or <code>-1</code> if the game
     * is not yet over. */
    public byte winningTeam;

    /**
     * Returns the number of active players in the given team.
     */
    public int getTeamActivePlayerCount (int teamid)
    {
        int count = 0;
        for (int ii = 0; ii < players.length; ii++) {
            if (teams[ii] == teamid && isActivePlayer(ii)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns a random valid target player for the given player, or
     * <code>-1</code> if there are no valid target players.
     */
    public int getRandomValidTarget (int pidx)
    {
        // if there's no valid target player, pull the ripcord
        int vcount = getValidTargetCount(pidx);
        if (vcount < 1) {
            if (isInPlay()) {
                Log.info("No valid targets [game=" + which() +
                         ", pidx=" + pidx +
                         ", status=" + StringUtil.toString(playerStatus) +
                         ", teams=" + StringUtil.toString(teams) + "].");
            }
            return -1;
        }

        // choose a random valid player and skip to their index
        int tpidx = 0, tcount = RandomUtil.getInt(vcount);
        do {
            // if this is a valid target and tcount is zero, stop; note
            // that we rely on the short-circuit to only decrement tcount
            // when we're on a valid target
            if (isRandomValidTarget(pidx, tpidx) && (tcount-- == 0)) {
                return tpidx;
            }
            tpidx++;
        } while (tpidx < players.length);

        // we should never get here, but it's best to be robust
        Log.warning("Failed to find valid target [game=" + which() +
                    ", pidx=" + pidx + ", vcount=" + vcount +
                    ", status=" + StringUtil.toString(playerStatus) +
                    ", teams=" + StringUtil.toString(teams) + "].");
        return -1;
    }

    /**
     * Returns the number of valid target players for the given player.
     */
    public int getValidTargetCount (int pidx)
    {
        int count = 0;
        for (int ii = 0; ii < players.length; ii++) {
            if (isRandomValidTarget(pidx, ii)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns whether one player may target another player.
     *
     * @param pidx the player index of the player attempting to change
     * their target player.
     * @param tpidx the player index of the player being targeted.
     */
    public boolean isValidTarget (int pidx, int tpidx)
    {
        return (tpidx >= 0 && tpidx < players.length &&
                pidx != tpidx && isActivePlayer(tpidx) &&
                teams[pidx] != teams[tpidx]);
    }
 
    /**
     * Returns whether one player may target another player on random 
     * reassignment.
     *
     * @param pidx the player index of the player attempting to change
     * their target player.
     * @param tpidx the player index of the player being targeted.
     */
    protected boolean isRandomValidTarget (int pidx, int tpidx)
    {
        return isValidTarget(pidx, tpidx);
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>teams</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setTeams (int[] value)
    {
        int[] ovalue = this.teams;
        requestAttributeChange(
            TEAMS, value, ovalue);
        this.teams = (value == null) ? null : (int[])value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>teams</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    public void setTeamsAt (int value, int index)
    {
        int ovalue = this.teams[index];
        requestElementUpdate(
            TEAMS, index, new Integer(value), new Integer(ovalue));
        this.teams[index] = value;
    }

    /**
     * Requests that the <code>targets</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setTargets (short[] value)
    {
        short[] ovalue = this.targets;
        requestAttributeChange(
            TARGETS, value, ovalue);
        this.targets = (value == null) ? null : (short[])value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>targets</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    public void setTargetsAt (short value, int index)
    {
        short ovalue = this.targets[index];
        requestElementUpdate(
            TARGETS, index, new Short(value), new Short(ovalue));
        this.targets[index] = value;
    }

    /**
     * Requests that the <code>winningTeam</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setWinningTeam (byte value)
    {
        byte ovalue = this.winningTeam;
        requestAttributeChange(
            WINNING_TEAM, new Byte(value), new Byte(ovalue));
        this.winningTeam = value;
    }
    // AUTO-GENERATED: METHODS END
}
