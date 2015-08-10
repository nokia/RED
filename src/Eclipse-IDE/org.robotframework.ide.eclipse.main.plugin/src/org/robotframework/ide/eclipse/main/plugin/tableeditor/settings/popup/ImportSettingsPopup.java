package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.red.jface.dialogs.InputLoadingFormComposite;
import org.robotframework.red.jface.dialogs.RobotPopupDialog;

public class ImportSettingsPopup extends RobotPopupDialog {

    private InputLoadingFormComposite composite;
    private final RobotEditorCommandsStack commandsStack;
    private final RobotSuiteFile fileModel;
    private RobotSetting initialSetting;

    public ImportSettingsPopup(final Shell parent, final RobotEditorCommandsStack commandsStack,
            final RobotSuiteFile fileModel, final RobotSetting initialSetting) {
        super(parent);
        this.commandsStack = commandsStack;
        this.fileModel = fileModel;
        this.initialSetting = initialSetting;
    }

    @Override
    protected Control createDialogControls(final Composite parent) {
        composite = new ImportSettingsComposite(parent, commandsStack, fileModel, initialSetting);
        return composite;
    }

    @Override
    protected Control getFocusControl() {
        return composite.getFocusControl();
    }
}
