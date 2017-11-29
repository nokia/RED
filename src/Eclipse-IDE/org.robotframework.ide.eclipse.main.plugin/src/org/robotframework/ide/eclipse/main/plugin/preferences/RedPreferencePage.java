package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;


abstract class RedPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    protected RedPreferencePage() {
        this("");
    }

    protected RedPreferencePage(final String title) {
        super(title);
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
