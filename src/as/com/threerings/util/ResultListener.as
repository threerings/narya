package com.threerings.util {

public interface ResultListener
{
    function requestCompleted (obj :Object) :void;

    function requestFailed (cause :Error) :void;
}
}
