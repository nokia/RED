package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class RobotPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

    public RobotPreferencesPage() {
        super("Main Robot preference page");
    }

    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected Control createContents(final Composite parent) {
        noDefaultAndApplyButton();
        return null;
    }

}
