//
// $Id: $


package com.threerings.admin.web.client;

import java.util.List;

import com.google.common.collect.Lists;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;

import com.threerings.admin.web.gwt.ConfigField;
import com.threerings.gwt.ui.InfoPopup;
import com.threerings.gwt.ui.SmartTable;

import com.threerings.admin.web.gwt.ConfigService.ConfigurationRecord;

import com.threerings.gwt.util.ClickCallback;

/**
 *
 */
public class ConfigEditorTab extends SmartTable
{
    public interface ConfigAccessor
    {
        void submitChanges (String key, ConfigField[] modified,
                            AsyncCallback<ConfigurationRecord> callback);
    }

    public ConfigEditorTab (ConfigAccessor parent, String key, ConfigurationRecord record)
    {
        super("configEditorTab", 5, 5);

        _parent = parent;
        _key = key;

        _submit = new Button("Submit Changes");

        // wire up saving the code on click
        new ClickCallback<ConfigurationRecord>(_submit) {
            protected boolean callService () {
                List<ConfigField> modified = Lists.newArrayList();
                for (ConfigFieldEditor editor : _editors) {
                    ConfigField field = editor.getModifiedField();
                    if (field != null) {
                        modified.add(field);
                    }
                }
                _parent.submitChanges(
                    _key, modified.toArray(new ConfigField[modified.size()]), this);
                return true;
            }
            protected boolean gotResult (ConfigurationRecord result) {
                new InfoPopup("Updated " + result.updates + " fields.").show();
                updateTable(result);
                return false;
            }
        };

        cell(1, 1).alignRight().widget(_submit);

        updateTable(record);
    }

    protected void updateTable (ConfigurationRecord record)
    {
        SmartTable table = new SmartTable(5, 5);
        table.setStyleName("configEditorTable");

        int row = 0;
        for (ConfigField field : record.fields) {
            ConfigFieldEditor editor = ConfigFieldEditor.getEditorFor(field, UPDATE_BUTTON);
            _editors.add(editor);
            table.cell(row, 0).alignRight().widget(editor.getNameWidget());
            table.cell(row, 1).alignLeft().widget(editor.getValueWidget());
            table.cell(row, 2).alignLeft().widget(editor.getResetWidget());
            row ++;
        }

        cell(0, 0).colSpan(2).widget(table);

        UPDATE_BUTTON.execute();
    }

    protected List<ConfigFieldEditor> _editors = Lists.newArrayList();

    protected ConfigAccessor _parent;
    protected String _key;
    protected Button _submit;

    protected Command UPDATE_BUTTON = new Command () {
        public void execute () {
            // search for any modified field; if found, enable submissions & exit
            for (ConfigFieldEditor editor : _editors) {
                if (editor.getModifiedField() != null) {
                    _submit.setEnabled(true);
                    return;
                }
            }
            _submit.setEnabled(false);
        }
    };
}
