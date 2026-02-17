//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.samskivert.util.QuickSort;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;

import com.threerings.presents.util.PresentsContext;

import static com.threerings.admin.Log.log;

/**
 * Fetches a list of the configuration objects in use by the server and
 * displays their fields in a tree widget to be viewed and edited.
 */
public class ConfigEditorPanel extends JPanel
    implements AdminService.ConfigInfoListener
{
    /**
     * Constructs an editor panel which will use the supplied context to
     * access the distributed object services.
     */
    public ConfigEditorPanel (PresentsContext ctx)
    {
        this(ctx, null);
    }

    /**
     * Constructs an editor panel with the specified pane defaulting to
     * selected.
     */
    public ConfigEditorPanel (PresentsContext ctx, String defaultPane)
    {
        _ctx = ctx;
        _defaultPane = defaultPane;

        setLayout(new VGroupLayout(VGroupLayout.STRETCH, VGroupLayout.STRETCH,
                                   VGroupLayout.DEFAULT_GAP, VGroupLayout.CENTER));

        // add a search bar at the top
        JPanel searchPanel = new JPanel(new HGroupLayout(HGroupLayout.STRETCH));
        searchPanel.add(new JLabel("Search: "), HGroupLayout.FIXED);
        searchPanel.add(_searchField = new JTextField());
        searchPanel.add(_matchLabel = new JLabel(), HGroupLayout.FIXED);
        add(searchPanel, VGroupLayout.FIXED);

        _searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate (DocumentEvent e) { filterTabs(); }
            public void removeUpdate (DocumentEvent e) { filterTabs(); }
            public void changedUpdate (DocumentEvent e) { filterTabs(); }
        });

        // bind Ctrl+F to focus the search field
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK), "focusSearch");
        getActionMap().put("focusSearch", new AbstractAction() {
            public void actionPerformed (ActionEvent e) {
                _searchField.requestFocusInWindow();
                _searchField.selectAll();
            }
        });

        // bind Escape to clear search and return focus
        _searchField.getInputMap().put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
        _searchField.getActionMap().put("clearSearch", new AbstractAction() {
            public void actionPerformed (ActionEvent e) {
                _searchField.setText("");
                _oeditors.requestFocusInWindow();
            }
        });

        // bind Enter / Shift+Enter to cycle through matching tabs
        _searchField.getInputMap().put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "nextMatch");
        _searchField.getActionMap().put("nextMatch", new AbstractAction() {
            public void actionPerformed (ActionEvent e) {
                cycleMatch(1);
            }
        });
        _searchField.getInputMap().put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.SHIFT_MASK), "prevMatch");
        _searchField.getActionMap().put("prevMatch", new AbstractAction() {
            public void actionPerformed (ActionEvent e) {
                cycleMatch(-1);
            }
        });

        // create our objects tabbed pane
        add(_oeditors = new JTabbedPane(JTabbedPane.LEFT));

        // If they don't fit, make them scroll, since wrapped vertical tabs eats insane sceen space
        _oeditors.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // add a handy label at the bottom
        add(new JLabel("Fields outline in red have been modified but not yet committed."),
            VGroupLayout.FIXED);
        add(new JLabel("Press return in a modified field to commit the change."),
            VGroupLayout.FIXED);
    }

    @Override
    public void addNotify ()
    {
        super.addNotify();

        // ship off a getConfigInfo request to find out what config
        // objects are available for editing
        AdminService service = _ctx.getClient().requireService(AdminService.class);
        service.getConfigInfo(this);
    }

    @Override
    public void removeNotify ()
    {
        super.removeNotify();

        // when we're hidden, we want to clear out our subscriptions
        int ccount = _oeditors.getComponentCount();
        for (int ii = 0; ii < ccount; ii++) {
            Component comp = _oeditors.getComponent(ii);
            if (comp instanceof JScrollPane) {
                JScrollPane scrolly = (JScrollPane)comp;
                ObjectEditorPanel opanel = (ObjectEditorPanel)scrolly.getViewport().getView();
                opanel.cleanup();
            }
        }
        _oeditors.removeAll();
    }

    /**
     * Called in response to our getConfigInfo server-side service request.
     */
    public void gotConfigInfo (final String[] keys, final int[] oids)
    {
        // make sure we're still added
        if (!isDisplayable()) {
            return;
        }

        Integer indexes[] = new Integer[keys.length];
        for (int ii = 0; ii < indexes.length; ii++) {
            indexes[ii] = ii;
        }

        QuickSort.sort(indexes, new Comparator<Integer>() {
            public int compare (Integer i1, Integer i2) {
                return keys[i1].compareTo(keys[i2]);
            }
        });

        // create object editor panels for each of the categories
        for (Integer ii : indexes) {
            ObjectEditorPanel panel = new ObjectEditorPanel(_ctx, keys[ii], oids[ii]);
            JScrollPane scrolly = new JScrollPane(panel);
            _oeditors.addTab(keys[ii], scrolly);
            if (keys[ii].equals(_defaultPane)) {
                _oeditors.setSelectedComponent(scrolly);
            }
        }
    }

    // documentation inherited from interface
    public void requestFailed (String reason)
    {
        log.warning("Failed to get config info", "reason", reason);
    }

    /** Filters tabs and highlights matching fields based on the current search text. */
    protected void filterTabs ()
    {
        String query = _searchField.getText().trim().toLowerCase();
        boolean empty = query.isEmpty();

        _matchingTabs.clear();
        int totalMatches = 0;
        for (int ii = 0; ii < _oeditors.getTabCount(); ii++) {
            boolean tabNameMatches = !empty &&
                _oeditors.getTitleAt(ii).toLowerCase().contains(query);
            int fieldMatchCount = highlightFields(ii, empty ? null : query);
            boolean matches = tabNameMatches || fieldMatchCount > 0;
            _oeditors.setForegroundAt(ii, (empty || matches) ? null : MISMATCH_COLOR);
            if (matches) {
                _matchingTabs.add(ii);
            }
            totalMatches += fieldMatchCount;
        }

        // update match count label
        if (empty) {
            _matchLabel.setText("");
        } else if (_matchingTabs.isEmpty()) {
            _matchLabel.setText("No matches");
        } else {
            _matchLabel.setText(totalMatches + " field" + (totalMatches != 1 ? "s" : "") +
                " in " + _matchingTabs.size() + " tab" + (_matchingTabs.size() != 1 ? "s" : ""));
        }

        _matchCursor = 0;
        if (!_matchingTabs.isEmpty()) {
            _oeditors.setSelectedIndex(_matchingTabs.get(0));
            scrollToFirstMatch();
        }
    }

    /**
     * Highlights matching field editors in the given tab. Returns the number of matching fields.
     * Pass null query to clear all highlights.
     */
    protected int highlightFields (int tabIndex, String query)
    {
        Component comp = _oeditors.getComponentAt(tabIndex);
        if (!(comp instanceof JScrollPane)) {
            return 0;
        }
        Component view = ((JScrollPane)comp).getViewport().getView();
        if (!(view instanceof ObjectEditorPanel)) {
            return 0;
        }

        int matchCount = 0;
        for (Component child : ((ObjectEditorPanel)view).getComponents()) {
            if (!(child instanceof FieldEditor)) {
                continue;
            }
            FieldEditor editor = (FieldEditor)child;
            boolean matches = query != null && fieldMatches(editor, query);
            editor.setOpaque(matches);
            editor.setBackground(matches ? HIGHLIGHT_COLOR : null);
            // Also highlight the label so the color is visible through the child components
            editor._label.setOpaque(matches);
            editor._label.setBackground(matches ? HIGHLIGHT_COLOR : null);
            editor.repaint();
            if (matches) {
                matchCount++;
            }
        }
        return matchCount;
    }

    /** Checks whether a field editor matches the search query by name or tooltip. */
    protected boolean fieldMatches (JPanel editor, String query)
    {
        if (editor instanceof FieldEditor) {
            if (((FieldEditor)editor)._field.getName().toLowerCase().contains(query)) {
                return true;
            }
        }
        String tip = editor.getToolTipText();
        return tip != null && tip.toLowerCase().contains(query);
    }

    /** Cycles to the next or previous matching tab. */
    protected void cycleMatch (int direction)
    {
        if (_matchingTabs.isEmpty()) {
            return;
        }
        _matchCursor = (_matchCursor + direction + _matchingTabs.size()) % _matchingTabs.size();
        _oeditors.setSelectedIndex(_matchingTabs.get(_matchCursor));
        scrollToFirstMatch();
    }

    /** Scrolls the currently selected tab's viewport to show the first matching field. */
    protected void scrollToFirstMatch ()
    {
        int tabIndex = _oeditors.getSelectedIndex();
        if (tabIndex < 0) {
            return;
        }
        Component comp = _oeditors.getComponentAt(tabIndex);
        if (!(comp instanceof JScrollPane)) {
            return;
        }
        Component view = ((JScrollPane)comp).getViewport().getView();
        if (!(view instanceof ObjectEditorPanel)) {
            return;
        }
        String query = _searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            return;
        }
        ObjectEditorPanel opanel = (ObjectEditorPanel)view;
        for (Component child : opanel.getComponents()) {
            if (child instanceof FieldEditor && fieldMatches((FieldEditor)child, query)) {
                opanel.scrollRectToVisible(child.getBounds());
                break;
            }
        }
    }

    /** Our client context. */
    protected PresentsContext _ctx;

    /** Holds our object editors. */
    protected JTabbedPane _oeditors;

    /** Our default tab pane. */
    protected String _defaultPane;

    /** Search field for filtering config tabs. */
    protected JTextField _searchField;

    /** Label showing the number of search matches. */
    protected JLabel _matchLabel;

    /** Indices of tabs that match the current search query. */
    protected List<Integer> _matchingTabs = new ArrayList<>();

    /** Current position in the matching tabs list for Enter/Shift+Enter cycling. */
    protected int _matchCursor;

    /** Color used for non-matching tabs. */
    protected static final Color MISMATCH_COLOR = Color.LIGHT_GRAY;

    /** Background color used for matching field editors. */
    protected static final Color HIGHLIGHT_COLOR = new Color(255, 255, 150);
}
