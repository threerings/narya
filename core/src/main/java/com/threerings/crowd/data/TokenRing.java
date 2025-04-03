//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.data;

import com.threerings.io.SimpleStreamableObject;

/**
 * Defines access control tokens that convey certain privileges to users.
 *
 * @see BodyObject.CrowdPermissionPolicy
 */
public class TokenRing extends SimpleStreamableObject
{
    /** Indicates that this user is an administrator. */
    public static final int ADMIN = (1 << 0);

    /**
     * Constructs a token ring with the supplied set of tokens.
     */
    public TokenRing (int tokens)
    {
        _tokens = tokens;
    }

    /**
     * Returns true if this token ring contains the specified token or tokens, exactly.  For
     * example, if you pass in the OR of two or more tokens, then the ring must contain all of
     * those tokens.
     */
    public boolean holdsToken (int token)
    {
        return (_tokens & token) == token;
    }

    /**
     * Returns true if this token ring contains any one of the specified tokens.
     */
    public boolean holdsAnyToken (int tokens)
    {
        return (_tokens & tokens) != 0;
    }

    /**
     * Convenience function for checking whether this ring holds the {@link #ADMIN} token.
     */
    public boolean isAdmin ()
    {
        return holdsToken(ADMIN);
    }

    /**
     * Returns the bitmask that stores the various tokens.
     */
    public int getTokens ()
    {
        return _tokens;
    }

    /**
     * Set the specified token to be on or off.
     */
    public void setToken (int token, boolean on)
    {
        if (on) {
            setToken(token);
        } else {
            clearToken(token);
        }
    }

    /**
     * Adds the specified token to this ring.
     */
    public void setToken (int token)
    {
        _tokens |= token;
    }

    /**
     * Clears the specified token from this ring.
     */
    public void clearToken (int token)
    {
        _tokens &= ~token;
    }

    /** The tokens contained in this ring (composed together bitwise). */
    protected int _tokens;
}
