//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

/**
 * A test of the invocation notification services.
 */
public interface TestReceiver extends InvocationReceiver
{
    /**
     * Dispatches a test notification.
     */
    public void receivedTest (int one, String two);
}
