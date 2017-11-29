package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;


abstract class RedFieldEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    protected RedFieldEditorPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
    }

    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return RedPlugin.getDefault().getPreferenceStore();
    }
}
