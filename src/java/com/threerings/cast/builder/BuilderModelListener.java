//
// $Id: BuilderModelListener.java,v 1.3 2001/11/27 08:09:35 mdb Exp $

package com.threerings.cast.builder;

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
