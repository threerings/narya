//
// $Id: ConfigObject.java,v 1.2 2004/03/06 12:00:39 mdb Exp $

package com.threerings.admin.data;

import java.lang.reflect.Field;
import javax.swing.JPanel;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.util.PresentsContext;

import com.threerings.admin.client.FieldEditor;

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
        return new FieldEditor(ctx, field, this);
    }
}
