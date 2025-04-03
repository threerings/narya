//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

import com.threerings.util.Name;

/**
 * Credentials that use a username and (hashed) password.
 */
public class UsernamePasswordCreds extends Credentials
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public UsernamePasswordCreds ()
    {
    }

    /**
     * Construct credentials with the supplied username and password.
     */
    public UsernamePasswordCreds (Name username, String password)
    {
        _username = username;
        _password = password;
    }

    public Name getUsername ()
    {
        return _username;
    }

    public String getPassword ()
    {
        return _password;
    }

    @Override
    public String getDatagramSecret ()
    {
        return _password;
    }

    @Override
    public String toString ()
    {
        StringBuilder buf = new StringBuilder("[");
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * An easily extensible method via which derived classes can add to {@link #toString}'s output.
     */
    protected void toString (StringBuilder buf)
    {
        buf.append("username=").append(_username);
        buf.append(", password=").append(_password);
    }

    protected Name _username;
    protected String _password;
}
