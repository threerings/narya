package com.threerings.presents.client {

public interface ResultListener extends InvocationListener
{
    function requestProcessed (result :Object) :void;
}
}
