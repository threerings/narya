//
// $Id: PerformanceObserver.java,v 1.1 2001/08/02 00:42:02 shaper Exp $

package com.threerings.miso.util;

/**
 * The <code>PerformanceObserver</code> interface should be
 * implemented by classes that wish to register actions to be
 * monitored by the <code>PerformanceMonitor</code> class.
 */
public interface PerformanceObserver
{
    /**
     * This method is called by the <code>PerformanceMonitor</code>
     * class whenever an action's requested time interval between
     * checkpoints has expired.
     *
     * @param name the action name.
     * @param ticks the ticks since the last checkpoint.
     */
    public void checkpoint (String name, int ticks);
}
