//
// $Id: EditorModelListener.java 217 2001-11-29 00:29:31Z mdb $

package com.threerings.stage.tools.editor;

/**
 * The editor model listener interface should be implemented by
 * classes that would like to be notified when the editor model is
 * changed.
 *
 * @see EditorModel
 */
public interface EditorModelListener
{
    /**
     * Called by the {@link EditorModel} when the model is changed.
     */
    public void modelChanged (int event);

    /** Notification event constants. */
    public static final int ACTION_MODE_CHANGED = 0;
    public static final int LAYER_INDEX_CHANGED = 1;
    public static final int TILE_CHANGED = 2;
}
