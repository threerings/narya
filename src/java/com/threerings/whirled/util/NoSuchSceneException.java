//
// $Id: NoSuchSceneException.java,v 1.1 2001/08/11 04:09:51 mdb Exp $

package com.threerings.whirled.util;

/**
 * Thrown when an attempt to load a non-existent scene is made on the
 * repository.
 */
public class NoSuchSceneException extends Exception
{
    public NoSuchSceneException (int sceneid)
    {
        super("No such scene [sceneid=" + sceneid + "]");
        _sceneid = sceneid;
    }

    public int getSceneId ()
    {
        return _sceneid;
    }

    protected int _sceneid;
}
