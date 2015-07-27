package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;

public class SuiteEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private static final String ID = "org.robotframework.ide.eclipse.main.plugin.preferences.editor";

    public SuiteEditorPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, RobotFramework.PLUGIN_ID));
        setDescription("RED suite editor settings");
    }

    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected void createFieldEditors() {
        final IntegerFieldEditor editor = new IntegerFieldEditor(RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS,
                "Default number of columns for arguments in table editors",
                getFieldEditorParent(), 2);
        editor.setValidRange(1, 20);
        addField(editor);
    }

}
