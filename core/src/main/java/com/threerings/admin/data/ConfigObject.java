//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.data;

import java.lang.reflect.Field;

import javax.swing.JPanel;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.util.PresentsContext;

import com.threerings.admin.client.AsStringFieldEditor;
import com.threerings.admin.client.BooleanFieldEditor;

/**
 * Base class for runtime config distributed objects.  Used to allow
 * config objects to supply custom object editing UI.
 */
public class ConfigObject extends DObject
{
    /**
     * Returns the editor panel for the specified field.
     */
    public JPanel getEditor (PresentsContext ctx, Field field)
    {
        if (field.getType().equals(Boolean.TYPE)) {
            return new BooleanFieldEditor(ctx, field, this);
        } else {
            return new AsStringFieldEditor(ctx, field, this);
        }
    }
}
