//
// $Id$

package com.threerings.util;

import com.threerings.util.unsafe.Unsafe;

/**
 * Does something extraordinary.
 */
public class UIDTest
{
    public static void main (String[] args)
    {
        if (Unsafe.setuid(1000)) {
            System.err.println("Yay! My uid is changed.");
        } else {
            System.err.println("Boo hoo! I couldn't change my uid.");
        }
        if (Unsafe.setgid(60)) {
            System.err.println("Yay! My gid is changed.");
        } else {
            System.err.println("Boo hoo! I couldn't change my gid.");
        }
        try {
            Thread.sleep(60*1000L);
        } catch (Exception e) {
        }
    }
}
