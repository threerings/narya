//
// $Id: MessageBundleTest.java,v 1.5 2004/08/27 02:21:05 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
