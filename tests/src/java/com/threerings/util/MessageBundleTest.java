//
// $Id: MessageBundleTest.java,v 1.1 2002/02/28 23:12:27 mdb Exp $

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

    public void runTest ()
    {
        try {
            ResourceBundle rbundle =
                ResourceBundle.getBundle("rsrc.i18n.messages");
            MessageBundle bundle = new MessageBundle("test", rbundle);

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
