//
// $Id: TileInfoPanel.java 17625 2004-10-28 17:50:03Z mdb $

package com.threerings.stage.tools.editor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.QuickSort;
import com.samskivert.util.StringUtil;

import com.threerings.media.SafeScrollPane;

import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileSetRepository;

import com.threerings.stage.tools.editor.util.EditorContext;
import com.threerings.stage.tools.editor.util.TileSetUtil;

/**
 * The tile info panel presents the user with options to select the
 * tile to be applied to the scene.
 */
public class TileInfoPanel extends JSplitPane
    implements ListSelectionListener, TreeSelectionListener
{
    /**
     * Constructs the tile info panel.
     */
    public TileInfoPanel (EditorContext ctx, EditorModel model)
    {
        TileSetRepository tsrepo = ctx.getTileSetRepository();

        // set up our key observers
        registerKeyListener(ctx);

        _model = model;

        // we're going to sort all of the available tilesets into those
        // which are applicable to each layer
        try {
            _layerSets = new ArrayList[2];
            _layerLengths = new int[2];
            for (int ii=0; ii < 2; ii++) {
                _layerSets[ii] = new ArrayList();
            }

            Iterator tsids = tsrepo.enumerateTileSetIds();
            while (tsids.hasNext()) {
                Integer tsid = (Integer)tsids.next();
                TileSet set = tsrepo.getTileSet(tsid.intValue());

                // determine which layer to which this tileset applies
                int lidx = TileSetUtil.getLayerIndex(set);
                if (lidx != -1) {
                    _layerSets[lidx].add(
                        new TileSetRecord(lidx, tsid.intValue(), set));
                }
            }

            for (int ii=0; ii < 2; ii++) {
                _layerLengths[ii] = _layerSets[ii].size();
            }

        } catch (Exception e) {
            Log.warning("Error enumerating tilesets.");
            Log.logStackTrace(e);
        }

        // set up a border denoting our contents
        Border border = BorderFactory.createEtchedBorder();
        setBorder(BorderFactory.createTitledBorder(border, "Tile Info"));

        // create a tree for selecting tileset
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        _tsettree = new JTree(root);

        // don't draw any funny little icons in the tree
        DefaultTreeCellRenderer cellrend =
            (DefaultTreeCellRenderer) _tsettree.getCellRenderer();
        cellrend.setLeafIcon(null);
        cellrend.setOpenIcon(null);
        cellrend.setClosedIcon(null);

        // tree- only let one thing be selected, and let us know when it haps
        _tsettree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
        _tsettree.addTreeSelectionListener(this);

        // create a scrollpane to hold the tree
        SafeScrollPane scrolly = new SafeScrollPane(_tsettree);
        scrolly.setVerticalScrollBarPolicy(
            SafeScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        DefaultListModel qmodel = new DefaultListModel();
        for (int ii=0; ii < 10; ii++) {
            qmodel.addElement("");
        }
        _quickList = new JList(qmodel);
        _quickList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _quickList.addListSelectionListener(this);
        _quickList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent (
                JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus)
            {
                // put the key number in front of each element
                Component result = super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                setText("" + ((index + 11) % 10) + ". " + getText());
                return result;
            }
        });
        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftSplit.setTopComponent(new SafeScrollPane(_quickList));
        leftSplit.setBottomComponent(scrolly);

        // add the west side
        setLeftComponent(leftSplit);

        // create a table to display the tiles in the selected tileset
        _tiletable = new JTable(_tablemodel = new TileTableModel());
        _tiletable.getSelectionModel().addListSelectionListener(this);
        _tiletable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateTileTable();

        // wrap the table in a scrollpane for lengthy tilesets
        _scroller = new SafeScrollPane(_tiletable);
        _scroller.setVerticalScrollBarPolicy(
            SafeScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // add the tile table as the entire east side
        setRightComponent(_scroller);

        // add the tilesets and select the starting tile set
        updateTileSetTree();

        // The damn splitpane freaks out unless we do this
        setDividerLocation(230);
    }

    /**
     * Register key listeners to do things with the quick list.
     */
    protected void registerKeyListener (EditorContext ctx)
    {
        ctx.getKeyDispatcher().addGlobalKeyListener(new KeyAdapter() {
            public void keyTyped (KeyEvent e)
            {
                char keychar = e.getKeyChar();
                if ((keychar < '0') || (keychar > '9')) {
                    return;
                }

                // turn 1 into 0, and 0 into 9
                int index = (keychar - '0' + 9) % 10;
                if (e.isControlDown() || e.isAltDown()) {
                    // add
                    if (_curTrec == null) {
                        return;
                    }
                    _quickList.clearSelection();
                    DefaultListModel model =
                        (DefaultListModel) _quickList.getModel();
                    int olddex = model.indexOf(_curTrec);
                    if (olddex != -1) {
                        model.set(olddex, "");
                    }
                    model.set(index, _curTrec);
                    _quickList.setSelectedIndex(index);

                } else {
                    // select
                    _quickList.setSelectedIndex(index);
                }
            }
        });
    }

    /**
     * Selects the previous tile in the list of available tiles.
     */
    public void selectPreviousTile ()
    {
        int row = _tiletable.getSelectedRow();
        if (--row >= 0) {
            _tiletable.setRowSelectionInterval(row, row);
        }
    }

    /**
     * Selects the next tile in the list of available tiles.
     */
    public void selectNextTile ()
    {
        int row = _tiletable.getSelectedRow();
        if (++row < _tiletable.getRowCount()) {
            _tiletable.setRowSelectionInterval(row, row);
        }
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
        return new Dimension(WIDTH, HEIGHT);
    }

    /**
     * Display the tiles in a tileset when it is selected.
     */
    public void valueChanged (TreeSelectionEvent e)
    {
        DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) _tsettree.getLastSelectedPathComponent();

        // we only care when a leaf is selected
        if ((node != null) && (node.isLeaf())) {

            _selected = node;

            Object uobj = node.getUserObject();
            if (!(uobj instanceof TileSetRecord)) {
                Log.info("Eh? Non-TileSetRecord leaf [obj=" + uobj +
                         ", class=" + StringUtil.shortClassName(uobj) + "].");
                return;
            }

            tileSetSelected((TileSetRecord) uobj);
            _quickList.clearSelection();
        }
    }

    /**
     * Called when a tileset is selected, either via the tree
     * or the recent list.
     */
    protected void tileSetSelected (TileSetRecord trec)
    {
        // if they've selected something new, update our tile display
        if (_model.getTileSet() != trec.tileSet) {
            _curTrec = trec;
            _model.setLayerIndex(trec.layer);

            // update the model to reflect new tile set and select tile
            // zero by default
            _model.setTile(trec.tileSet, trec.tileSetId, 0);

            // update the tile table to reflect the new tileset
            updateTileTable();

//            _quickList.removeListSelectionListener(this);
//            // add it to the recent list
//            DefaultListModel recentModel = (DefaultListModel)
//                _quickList.getModel();
//            recentModel.removeElement(trec);
//            recentModel.add(0, trec);
//            _quickList.setSelectedIndex(0);
//            _quickList.addListSelectionListener(this);
        }
    }

    /**
     * Remove previous test tiles and insert the new batch.
     */
    protected void insertTestTiles (HashIntMap tests)
    {
        // trim the tilesets back to remove any previous test tiles
        for (int ii=0; ii < 2; ii++) {
            for (int jj=_layerSets[ii].size() - 1; jj >= _layerLengths[ii];
                    jj--) {
                _layerSets[ii].remove(jj);
            }
        }

        // insert the new test tiles
        Iterator iter = tests.keys();
        while (iter.hasNext()) {
            Integer tsid = (Integer) iter.next();

            TileSet set = (TileSet) tests.get(tsid);

            // determine which layer to which this tileset applies
            int lidx = TileSetUtil.getLayerIndex(set);
            if (lidx != -1) {
                // make up a negative number to refer to this temporary tileset
                _layerSets[lidx].add(
                    new TileSetRecord(lidx, tsid.intValue(), set));
            }
        }

        updateTileSetTree();
    }

    /**
     * The layer has changed, update the tree to reflect the tilesets
     * now available.
     */
    public void updateTileSetTree ()
    {
        // first clear out the tree
        DefaultTreeModel model = (DefaultTreeModel) _tsettree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        root.removeAllChildren();

        ArrayList expand = new ArrayList();

        // add all the elements in the base layer
        DefaultMutableTreeNode base = new DefaultMutableTreeNode("Base Layer");
        root.add(base);
        addNodes(base, getSortedTileSets(EditorModel.BASE_LAYER),
                 "", 0, expand);

        // add all the elements in the object layer
        DefaultMutableTreeNode obj = new DefaultMutableTreeNode("Object Layer");
        root.add(obj);
        addNodes(obj, getSortedTileSets(EditorModel.OBJECT_LAYER),
                 "", 0, expand);

        // notify the JTree that we've put some brand new branches on it.
        model.reload();

        // expand our container categories
        for (Iterator iter = expand.iterator(); iter.hasNext(); ) {
            _tsettree.expandPath((TreePath)iter.next());
        }

        // now select the previously selected item, or the first...
        if (_selected == null) {
            _selected = (DefaultMutableTreeNode) root.getFirstLeaf();
        }
        _tsettree.setSelectionPath(new TreePath(_selected.getPath()));
    }

    /**
     * Populate the tree with the available tilesets for the selected layer.
     */
    protected TileSetRecord[] getSortedTileSets (int layer)
    {
        // get the list of tilesets we now want to show
        ArrayList sets = _layerSets[layer];

        // we don't want to sort the actual array since we have
        // kept the test tiles at the end
        TileSetRecord[] sorted = new TileSetRecord[sets.size()];
        sets.toArray(sorted);
        QuickSort.sort(sorted);

        return sorted;
    }

    /**
     * Recursively add tilesets to the tree.
     *
     * @param prefix The portion of the full tileset name that we've
     * already parsed, it corresponds to the node we're adding to.
     * @param position The position in the array from whence to start adding.
     * @return the number of elements added to 'node' from 'list'.
     */
    protected int addNodes (DefaultMutableTreeNode node,
                            TileSetRecord[] list, String prefix, int position,
                            ArrayList expand)
    {
        int prefixlen = prefix.length();

        for (int ii = position; ii < list.length; ) {
            String name = list[ii].fullname();

            // if the next name on the list doesn't start with the prefix,
            // we have no business adding it to this node.
            if (!name.startsWith(prefix)) {
                return ii - position;
            }

            // is there another category name?
            int dex = name.indexOf('/', prefixlen);
            if (dex == -1) {
                // nope, just add this item to the node.
                DefaultMutableTreeNode item = new DefaultMutableTreeNode(
                    list[ii]);
                node.add(item);

                // oh, we're so sneaky!
                // if the item we're adding has the same TileSetRecord
                // as the previously selected item, we're going to want to
                // select it..
                if ((_selected != null) &&
                    (list[ii].equals(_selected.getUserObject()))) {
                    _selected = item;
                }

                ii++;

            } else {
                // new category!
                String catname = name.substring(prefixlen, dex);
                DefaultMutableTreeNode category =
                    new DefaultMutableTreeNode(catname);
                node.add(category);

                // if we have further categories below, start expanded
                if (name.indexOf('/', dex+1) != -1) {
                    expand.add(new TreePath(category.getPath()));
                }

                // recurse..
                ii += addNodes(category, list, name.substring(0, dex + 1),
                               ii, expand);
            }
        }

        return list.length - position;
    }

    /**
     * Update the tile table to reflect the currently selected tile set.
     */
    protected void updateTileTable ()
    {
        // get the table width before we update the table model since
        // updating the model seems to reset the table width to an
        // incorrect default
        TableColumn tcol = _tiletable.getColumnModel().getColumn(0);
        _tablewid = tcol.getWidth() - (2 * EDGE_TILE_H);

        // clear out the old selection because we're going to change
        // tilesets
        _tiletable.clearSelection();

        // update the table model with the new tile set tiles
        _tablemodel.updateTileSet();

        // if there are no tiles in the current tile set, we're done
        if (!_model.isTileValid()) {
            return;
        }

        // set row heights to match the scaled tile image heights
        int numTiles = getTileCount();
        TileSet set = _model.getTileSet();
        for (int ii = 0; ii < numTiles; ii++) {
            Image img = set.getRawTileImage(ii);
            int hei = getScaledTileImageHeight(img);
            _tiletable.setRowHeight(ii, hei + (2 * EDGE_TILE_V));
        }

        // select the selected tile
        int tid = _model.getTileId();
            _tiletable.setRowSelectionInterval(tid, tid);

        if (_scroller != null) {
            // scroll to the selected tile
            Rectangle r = _tiletable.getCellRect(tid, 0, true);
            _scroller.getViewport().setViewPosition(new Point(r.x, r.y));
        }
    }

    /**
     * Handle tile table selections.
     */
    public void valueChanged (ListSelectionEvent e)
    {
        // ignore extra messages
        if (e.getValueIsAdjusting()) {
            return;
        }

        Object src = e.getSource();
        if (src == _quickList) {
            Object o = _quickList.getSelectedValue();
            if (o instanceof TileSetRecord) {
                tileSetSelected((TileSetRecord) o);
                if (o != _selected.getUserObject()) {
                    _tsettree.clearSelection();
                }
            }
        } else {
            // otherwise they clicked on the tile table.
            ListSelectionModel lsm = (ListSelectionModel) src;
            if (!lsm.isSelectionEmpty()) {
                _model.setTileId(lsm.getMinSelectionIndex());
            }
        }
    }

    /**
     * Returns the number of tiles in the currently selected tileset.
     */
    protected int getTileCount ()
    {
        if (!_model.isTileValid()) {
            return 0;

        } else {
            TileSet set = _model.getTileSet();
            return (set == null) ? 0 : set.getTileCount();
        }
    }

    /**
     * Returns the height of the given tile image after scaling to fit
     * within the width of the tile table.
     */
    protected int getScaledTileImageHeight (Image img)
    {
        int wid = img.getWidth(null), hei = img.getHeight(null);
        if (wid > _tablewid) {
            float frac = (float)wid / (float)_tablewid;
            return (int)(hei / frac);
        }

        return hei;
    }

    /**
     * Extends the {@link AbstractTableModel} to encapsulate the table
     * layout and display options required when displaying the tiles in
     * the currently selected tileset.
     */
    protected class TileTableModel extends AbstractTableModel
    {
        /**
         * Called when the tile set associated with the table has been
         * changed.  Clears the cached image icons used to display each
         * cell and updates the number of rows in the table to properly
         * deal with tile sets of varying sizes.
         */
        public synchronized void updateTileSet ()
        {
            int numTiles = getTileCount();
            _icons = new ImageIcon[numTiles];
            fireTableRowsInserted(0, numTiles);
        }

        // documentation inherited
        public int getColumnCount ()
        {
            return 1;
        }

        // documentation inherited
        public String getColumnName (int columnIndex)
        {
            return null;
        }

        // documentation inherited
        public int getRowCount ()
        {
            return getTileCount();
        }

        // documentation inherited
        public Object getValueAt (int row, int col)
        {
            // return the icon immediately if it's already cached
            if (_icons[row] != null) {
                return _icons[row];
            }

            // generate and save off the tile image scaled to fit the table
            TileSet set = _model.getTileSet();
            Image img = set.getRawTileImage(row);
            int hei = getScaledTileImageHeight(img);

            if (hei != img.getHeight(null)) {
                img = img.getScaledInstance(
                    _tablewid, hei, Image.SCALE_SMOOTH);
            }

            return (_icons[row] = new ImageIcon(img));
        }

        // documentation inherited
        public Class getColumnClass (int c)
        {
            // return the object associated with the column to force
            // rendering of our icon images rather than straight text
            return getValueAt(0, c).getClass();
        }

        /** The image icons used to display the table cell contents. */
        protected ImageIcon _icons[];
    }

    /**
     * Used to manage tilesets in the tileset selection combobox.
     */
    protected static class TileSetRecord implements Comparable
    {
        public int layer;
        public int tileSetId;
        public TileSet tileSet;
        public String shortname;

        public TileSetRecord (int layer, int tileSetId, TileSet tileSet)
        {
            this.layer = layer;
            this.tileSetId = tileSetId;
            this.tileSet = tileSet;

            shortname = fullname();

            // cut everything before the last slash for our shortname
            int lastdex = shortname.lastIndexOf('/');
            if (lastdex != -1) {
                shortname = shortname.substring(lastdex + 1);
            }
        }

        public String fullname ()
        {
            return tileSet.getName();
        }

        public String toString ()
        {
            return shortname;
        }

        public int compareTo (Object o)
        {
            return fullname().compareToIgnoreCase(
                ((TileSetRecord) o).fullname());
        }

        public boolean equals (Object o)
        {
            if (o instanceof TileSetRecord) {
                TileSetRecord tsr = (TileSetRecord) o;
                return ((tsr.layer == layer) && (tsr.tileSetId == tileSetId));
            }
            return false;
        }
    }

    /** Default desired panel dimensions. */
    protected static final int WIDTH = 400;
    protected static final int HEIGHT = 300;

    /** Buffer space surrounding each tile in the tile table. */
    protected static final int EDGE_TILE_H = 4;
    protected static final int EDGE_TILE_V = 4;

    /** An ArrayList of TileSetRecords for each layer. */
    protected ArrayList[] _layerSets;

    /** The original number of TileSetRecords for each layer. */
    protected int[] _layerLengths;

    /** The tree listing available tilesets. */
    protected JTree _tsettree;

    /** The selected tree node. */
    protected DefaultMutableTreeNode _selected;

    /** The table listing all tiles in the selected tileset. */
    protected JTable _tiletable;

    /** The list of quickly-selectable tilesets. */
    protected JList _quickList;

    /** The currently selected tileset record. */
    protected TileSetRecord _curTrec;

    /** The width of the tile table column in pixels. */
    protected int _tablewid;

    /** The scroll pane containing the tile table. */
    protected SafeScrollPane _scroller;

    /** The editor model. */
    protected EditorModel _model;

    /** The tile table data model. */
    protected TileTableModel _tablemodel;
}
