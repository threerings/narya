//
// $Id: MessageBundleTest.java,v 1.3 2002/11/12 22:56:18 shaper Exp $

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
            String path = "rsrc.i18n.messages";
            ResourceBundle rbundle = ResourceBundle.getBundle(path);
            MessageBundle bundle = new MessageBundle();
            bundle.init("test", rbundle, null);

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
