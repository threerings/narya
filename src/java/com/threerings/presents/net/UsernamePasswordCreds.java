//
// $Id: UsernamePasswordCreds.java,v 1.11 2002/12/20 23:41:26 mdb Exp $

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

    public String toString ()
    {
        return "[username=" + _username + ", password=" + _password + "]";
    }

    protected String _password;
}
