//
// $Id: PerformanceObserver.java,v 1.2 2001/10/25 22:08:29 mdb Exp $

package com.threerings.media.util;

/**
 * This interface should be implemented by classes that wish to register
 * actions to be monitored by the {@link PerformanceMonitor} class.
 */
public interface PerformanceObserver
{
    /**
     * This method is called by the {@link PerformanceMonitor} class
     * whenever an action's requested time interval between checkpoints
     * has expired.
     *
     * @param name the action name.
     * @param ticks the ticks since the last checkpoint.
     */
    public void checkpoint (String name, int ticks);
}
