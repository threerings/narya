//
// $Id: ObjectEditorPanel.java,v 1.3 2004/03/04 02:43:48 eric Exp $

package com.threerings.admin.client;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;

import com.samskivert.swing.ScrollablePanel;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.event.AncestorAdapter;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.util.PresentsContext;

import com.threerings.admin.Log;

/**
 * Used to edit the distributed object fields of a particular
 * configuration object.  When the panel is first shown, it will subscribe
 * to the object and display its fields. It will not automatically
 * unsubscribe when it is hidden, but rather {@link #cleanup} must be
 * called to let it know that it's not going to be shown again soon and it
 * is safe for it to clear out its subscription.
 *
 * @see ConfigEditorPanel
 */
public class ObjectEditorPanel extends ScrollablePanel
    implements Subscriber
{
    /**
     * Creates an object editor panel for the specified configuration
     * object.
     */
    public ObjectEditorPanel (PresentsContext ctx, String key, int oid)
    {
        super(new VGroupLayout(VGroupLayout.NONE, VGroupLayout.STRETCH,
                               5, VGroupLayout.TOP));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // keep this business around
        _ctx = ctx;
        _key = key;
        _oid = oid;

        // when we're hidden, we want to clear out our subscriptions
        addAncestorListener(new AncestorAdapter () {
            public void ancestorAdded (AncestorEvent event) {
                // subscribe to our object the first time we're shown
                if (_object == null) {
                    _ctx.getDObjectManager().subscribeToObject(
                        _oid, ObjectEditorPanel.this);
                }
            }
        });
    }

    // documentation inherited from interface
    public boolean getScrollableTracksViewportWidth ()
    {
        return true;
    }

    /**
     * This method must be called to let the object editor panel know that
     * it's OK for it to remove its subscription to its config object.
     */
    public void cleanup ()
    {
        if (_object != null) {
            _ctx.getDObjectManager().unsubscribeFromObject(_oid, this);
            _object = null;
        }

        // clear out our field editors
        removeAll();
    }

    // documentation inherited from interface
    public void objectAvailable (DObject object)
    {
        // keep this for later
        _object = (ConfigObject)object;

        // create our field editors
        try {
            Field[] fields = object.getClass().getFields();
            for (int ii = 0; ii < fields.length; ii++) {
                // if the field is anything but a plain old public field,
                // we don't want to edit it
                if (fields[ii].getModifiers() == Modifier.PUBLIC) {
                    JPanel panel = _object.getCustomEditor(_ctx,
                                                           fields[ii].getName());
                    if (panel == null) {
                        panel = new FieldEditor(_ctx, fields[ii], _object);
                    }
                    add(panel);
                }
            }

        } catch (SecurityException se) {
            Log.warning("Unable to introspect DObject!? " + se);
        }

        SwingUtil.refresh(this);
    }

    // documentation inherited from interface
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        Log.warning("Unable to subscribe to config object: " + cause);
    }

    protected PresentsContext _ctx;
    protected String _key;
    protected int _oid;
    protected ConfigObject _object;
}
