//
// $Id: UsernamePasswordCreds.java,v 1.12 2004/01/31 12:16:12 mdb Exp $

package com.threerings.presents.net;

public class UsernamePasswordCreds extends Credentials
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public UsernamePasswordCreds ()
    {
        super();
    }

    /**
     * Construct credentials with the supplied username and password.
     */
    public UsernamePasswordCreds (String username, String password)
    {
        super(username);
        _password = password;
    }

    public String getPassword ()
    {
        return _password;
    }

    // documentation inherited
    public int hashCode ()
    {
        return super.hashCode() ^ _password.hashCode();
    }

    // documentation inherited
    public boolean equals (Object other)
    {
        if (other instanceof UsernamePasswordCreds) {
            UsernamePasswordCreds upcreds = (UsernamePasswordCreds)other;
            return super.equals(other) &&
                _password.equals(upcreds._password);
        } else {
            return false;
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", password=").append(_password);
    }

    protected String _password;
}
