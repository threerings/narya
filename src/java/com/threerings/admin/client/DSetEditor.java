//
// $Id: DSetEditor.java,v 1.4 2004/06/14 01:09:23 ray Exp $

package com.threerings.admin.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.BitSet;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.samskivert.swing.ObjectEditorTable;
import com.samskivert.swing.event.CommandEvent;

import com.samskivert.util.ClassUtil;
import com.samskivert.util.ListUtil;

import com.threerings.media.SafeScrollPane;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;

/**
 * Allows simple editing of DSets withing a distributed object.
 */
public class DSetEditor extends JPanel
    implements SetListener, ActionListener
{
    /**
     * Construct a DSet editor to merely display the specified set.
     *
     * @param setter The object that contains the set.
     * @param setName The name of the set in the object.
     * @param entryClass the Class of the DSet.Entry elements contained in the
     *                   set.
     */
    public DSetEditor (DObject setter, String setName, Class entryClass)
    {
        this(setter, setName, entryClass, null);
    }

    /**
     * Construct a DSetEditor, allowing the specified fields to be edited.
     *
     * @param setter The object that contains the set.
     * @param setName The name of the set in the object.
     * @param entryClass the Class of the DSet.Entry elements contained in the
     *                   set.
     * @param editableFields the names of the fields in the entryClass that
     *                       should be editable.
     */
    public DSetEditor (DObject setter, String setName, Class entryClass,
                       String[] editableFields)
    {
        this(setter, setName, entryClass, editableFields, null);
    }

    /**
     * Construct a DSetEditor with a custom FieldInterpreter.
     *
     * @param setter The object that contains the set.
     * @param setName The name of the set in the object.
     * @param entryClass the Class of the DSet.Entry elements contained in the
     *                   set.
     * @param editableFields the names of the fields in the entryClass that
     *                       should be editable.
     * @param interp The FieldInterpreter to use.
     */
    public DSetEditor (DObject setter, String setName, Class entryClass,
                       String[] editableFields,
                       ObjectEditorTable.FieldInterpreter interp)
    {
        super(new BorderLayout());

        _setter = setter;
        _setName = setName;
        _set = _setter.getSet(setName);

        _table = new ObjectEditorTable(entryClass, editableFields, interp);

        add(new SafeScrollPane(_table), BorderLayout.CENTER);
    }

    /**
     * Get the table being used to display the set.
     */
    public JTable getTable ()
    {
        return _table;
    }

    /**
     * Get the currently selected entry.
     */
    public DSet.Entry getSelectedEntry ()
    {
        return (DSet.Entry) _table.getSelectedObject();
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
        Dimension d = super.getPreferredSize();
        d.height = Math.min(d.height, 200);
        return d;
    }

    // documentation inherited
    public void addNotify ()
    {
        super.addNotify();
        _setter.addListener(this);
        _table.addActionListener(this);
        makeData();
    }

    // documentation inherited
    public void removeNotify ()
    {
        _setter.removeListener(this);
        _table.removeActionListener(this);
        super.removeNotify();
    }

    protected void makeData ()
    {
        _table.setData(_set.toArray(null));
    }

    // documentation inherited from interface SetListener
    public void entryAdded (EntryAddedEvent event)
    {
        if (event.getName().equals(_setName)) {
            makeData(); // recreate the entire shebang
        }
    }

    // documentation inherited from interface SetListener
    public void entryRemoved (EntryRemovedEvent event)
    {
        if (event.getName().equals(_setName)) {
            makeData(); // recreate the entire shebang
        }
    }

    // documentation inherited from interface SetListener
    public void entryUpdated (EntryUpdatedEvent event)
    {
        if (event.getName().equals(_setName)) {
            makeData(); // recreate the entire shebang
        }
    }

    // documentation inherited from interface ActionListener
    public void actionPerformed (ActionEvent event)
    {
        CommandEvent ce = (CommandEvent) event;
        _setter.updateSet(_setName, (DSet.Entry) ce.getArgument());
    }

    /** The object that contains the set we're displaying. */
    protected DObject _setter;

    /** The name of the set in that object. */
    protected String _setName;

    /** The set itself. */
    protected DSet _set;

    /** The table used to edit. */
    protected ObjectEditorTable _table;
}
