//
// $Id: DSetEditor.java,v 1.2 2004/06/03 18:15:03 ray Exp $

package com.threerings.admin.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.lang.reflect.Field;
import java.util.BitSet;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

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
{
    /**
     * The default FieldInterpreter, which can be used to customize the
     * name, values, and editing of a field in a DSet.Entry.
     *
     * There are a number of ways that the field editing can be customized.
     * A custom renderer (and editor) may be installed on the table, possibly
     * in conjunction with overriding getClass(). Or you may simply override
     * getValue (and setValue) to interpret between types, say for instance
     * turning an integer field that may be one of three constant values into
     * String names of the values.
     */
    public static class FieldInterpreter
    {
        /**
         * Get the name that she be used for the column header for the specified
         * field. By default it's merely the name of the field.
         */
        public String getName (Field field)
        {
            return field.getName();
        }

        /**
         * Get the class of the specified field. By default, the class of
         * the field is used, or its object equivalent if it is a primitive
         * class.
         */
        public Class getClass (Field field)
        {
            Class clazz = field.getType();
            return ClassUtil.objectEquivalentOf(clazz);
        }

        /**
         * Get the value of the specified field in the specified object.
         * By default, the field is used to directly access the value.
         */
        public Object getValue (Object obj, Field field)
        {
            try {
                return field.get(obj);
            } catch (IllegalAccessException iae) {
                Log.logStackTrace(iae);
                return null;
            }
        }

        /**
         * Set the value of the specified field in the specified object.
         * By default, the field is used to directly set the value.
         */
        public void setValue (Object obj, Object value, Field field)
        {
            try {
                field.set(obj, value);
            } catch (IllegalAccessException iae) {
                Log.logStackTrace(iae);
            }
        }
    }

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
                       String[] editableFields, FieldInterpreter interp)
    {
        super(new BorderLayout());

        _setter = setter;
        _setName = setName;
        _set = _setter.getSet(setName);
        _interp = (interp != null) ? interp : new FieldInterpreter();

        _fields = ClassUtil.getFields(entryClass);
        for (int ii=0, nn=_fields.length; ii < nn; ii++) {
            if (ListUtil.contains(editableFields, _fields[ii].getName())) {
                _editable.set(ii);
            }
        }

        _table = new JTable(_model = new EntryTableModel());
        add(new SafeScrollPane(_table), BorderLayout.CENTER);
    }

    /**
     * Get the table being used to display the set.
     */
    public JTable getTable ()
    {
        return _table;
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
        _setter.addListener(_model);
    }

    // documentation inherited
    public void removeNotify ()
    {
        super.removeNotify();
        _setter.removeListener(_model);
        _model.startup();
    }

    public class EntryTableModel extends AbstractTableModel
        implements SetListener
    {
        public void startup ()
        {
            fireTableDataChanged();
        }

        // documentation inherited from interface SetListener
        public void entryAdded (EntryAddedEvent event)
        {
            if (event.getName().equals(_setName)) {
                fireTableDataChanged(); // sloppy, but not too bad.
            }
        }

        // documentation inherited from interface SetListener
        public void entryRemoved (EntryRemovedEvent event)
        {
            if (event.getName().equals(_setName)) {
                fireTableDataChanged(); // sloppy, but not too bad.
            }
        }

        // documentation inherited from interface SetListener
        public void entryUpdated (EntryUpdatedEvent event)
        {
            if (event.getName().equals(_setName)) {
                fireTableDataChanged(); // sloppy, but not too bad.
            }
        }

        // documentation inherited
        public int getColumnCount ()
        {
            return _fields.length;
        }

        // documentation inherited
        public int getRowCount ()
        {
            return _set.size();
        }

        // documentation inherited
        public String getColumnName (int col)
        {
            return _interp.getName(_fields[col]);
        }

        // documentation inherited
        public boolean isCellEditable (int row, int col)
        {
            return _editable.get(col);
        }

        // documentation inherited
        public Class getColumnClass (int col)
        {
            return _interp.getClass(_fields[col]);
        }

        // documentation inherited
        public Object getValueAt (int row, int col)
        {
            DSet.Entry entry = getEntry(row);
            return _interp.getValue(entry, _fields[col]);
        }

        // documentation inherited
        public void setValueAt (Object value, int row, int col)
        {
            DSet.Entry entry = getEntry(row);
            _interp.setValue(entry, value, _fields[col]);
            _setter.updateSet(_setName, entry);
        }

        /**
         * Get a particular entry in the set.
         * TODO: check safety of this.
         */
        protected DSet.Entry getEntry (int row)
        {
            DSet.Entry[] vals = new DSet.Entry[_set.size()];
            _set.toArray(vals);
            return vals[row];
        }
    }

    /** The object that contains the set we're displaying. */
    protected DObject _setter;

    /** The name of the set in that object. */
    protected String _setName;

    /** The set itself. */
    protected DSet _set;

    /** The list of fields in the prototypical Entry object. */
    protected Field[] _fields;

    /** An interpreter that is used to massage values in and out of the
     * entries. */
    protected FieldInterpreter _interp;

    /** The table used to edit. */
    protected JTable _table;

    /** The model of the table used to edit the set. */
    protected EntryTableModel _model;

    /** A list of flags corresponding to the _fields (and the table columns)
     * that indicate if the field is editable. */
    protected BitSet _editable = new BitSet();
}
