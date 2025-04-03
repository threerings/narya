//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.client;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.swing.BorderFactory;

import com.samskivert.swing.ScrollablePanel;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.util.PresentsContext;
import com.threerings.presents.util.SafeSubscriber;

import com.threerings.admin.data.ConfigObject;

import static com.threerings.admin.Log.log;

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
    implements Subscriber<ConfigObject>
{
    /**
     * Creates an object editor panel for the specified configuration
     * object.
     */
    public ObjectEditorPanel (PresentsContext ctx, String key, int oid)
    {
        super(new VGroupLayout(VGroupLayout.NONE, VGroupLayout.STRETCH,
                               VGroupLayout.DEFAULT_GAP, VGroupLayout.TOP));
        setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));

        // keep this business around
        _ctx = ctx;
        _key = key;

        // we'll use this to safely subscribe to and unsubscribe from the
        // config object
        _safesub = new SafeSubscriber<ConfigObject>(oid, this);
        _safesub.subscribe(_ctx.getDObjectManager());
    }

    @Override
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
        // clear out our subscription
        _safesub.unsubscribe(_ctx.getDObjectManager());
        _object = null;

        // clear out our field editors
        removeAll();
    }

    // documentation inherited from interface
    public void objectAvailable (ConfigObject object)
    {
        // keep this for later
        _object = object;

        // create our field editors
        try {
            Field[] fields = object.getClass().getFields();
            for (Field field : fields) {
                // if the field is anything but a plain old public field,
                // we don't want to edit it
                if (field.getModifiers() == Modifier.PUBLIC) {
                    add(_object.getEditor(_ctx, field));
                }
            }

        } catch (SecurityException se) {
            log.warning("Unable to introspect DObject!? " + se);
        }

        SwingUtil.refresh(this);
    }

    // documentation inherited from interface
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        log.warning("Unable to subscribe to config object: " + cause);
    }

    protected PresentsContext _ctx;
    protected String _key;
    protected SafeSubscriber<ConfigObject> _safesub;
    protected ConfigObject _object;

    protected static final int BORDER = 5;
}
