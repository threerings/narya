//
// $Id: BuilderModelListener.java,v 1.2 2001/11/18 04:09:21 mdb Exp $

package com.threerings.cast.tools.builder;

/**
 * The builder model listener interface should be implemented by
 * classes that would like to be notified when the builder model is
 * changed.
 *
 * @see BuilderModel
 */
public interface BuilderModelListener
{
    /**
     * Called by the {@link BuilderModel} when the model is changed.
     */
    public void modelChanged (int event);

    /** Notification event constants. */
    public static final int COMPONENT_CHANGED = 0;
}
