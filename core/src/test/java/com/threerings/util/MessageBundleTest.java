//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.util;

import java.util.ResourceBundle;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Tests the {@link MessageBundle} class.
 */
public class MessageBundleTest extends TestCase
{
    public MessageBundleTest ()
    {
        super(MessageBundleTest.class.getName());
    }

    @Override
    public void runTest ()
    {
        try {
            String path = "i18n.messages";
            ResourceBundle rbundle = ResourceBundle.getBundle(path);
            MessageBundle bundle = new MessageBundle();
            bundle.init(null, "test", rbundle, null);

            String key1 = MessageBundle.compose("m.foo",
                                                MessageBundle.taint("bar"),
                                                MessageBundle.taint("baz"));
            String key2 = MessageBundle.compose("m.biff",
                                                MessageBundle.taint("beep"),
                                                MessageBundle.taint("boop"));
            String key = MessageBundle.compose("m.meta", key1, key2);

            String output = bundle.xlate(key);
            if (!OUTPUT.equals(output)) {
                fail("xlate failed: " + output);
            }

            // Counting sheep
            assertEquals("No sheep",
                bundle.xlate(MessageBundle.compose("m.sheep", MessageBundle.taint(0))));
            assertEquals("One sheep",
                bundle.xlate(MessageBundle.compose("m.sheep", MessageBundle.taint(1))));
            assertEquals("666 sheeps",
                bundle.xlate(MessageBundle.compose("m.sheep", MessageBundle.taint("666"))));
            assertEquals("Don't suffix me",
                bundle.xlate(MessageBundle.compose("m.sheep", MessageBundle.taint("zzz"))));

            assertEquals("7x12", bundle.xlate(MessageBundle.compose("m.coord",
                MessageBundle.taint(7), MessageBundle.taint(12))));

        } catch (Exception e) {
            fail("Test failed: " + e);
        }
    }

    public static Test suite ()
    {
        return new MessageBundleTest();
    }

    public static void main (String[] args)
    {
        MessageBundleTest test = new MessageBundleTest();
        test.runTest();
    }

    protected static final String OUTPUT =
        "Meta arg one is 'Foo arg one is 'bar' and two is 'baz'.' and " +
        "two is 'Biff arg one is 'beep' and two is 'boop'.'.";
}
