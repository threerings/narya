//
// $Id$
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

package com.threerings.admin.client;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.samskivert.swing.ScrollablePanel;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.util.PresentsContext;
import com.threerings.presents.util.SafeSubscriber;

import com.threerings.admin.Log;
import com.threerings.admin.data.ConfigObject;

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

        // we'll use this to safely subscribe to and unsubscribe from the
        // config object
        _safesub = new SafeSubscriber(oid, this);
        _safesub.subscribe(_ctx.getDObjectManager());
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
        // clear out our subscription
        _safesub.unsubscribe(_ctx.getDObjectManager());
        _object = null;

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
                    add(_object.getEditor(_ctx, fields[ii]));
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
    protected SafeSubscriber _safesub;
    protected ConfigObject _object;
}
