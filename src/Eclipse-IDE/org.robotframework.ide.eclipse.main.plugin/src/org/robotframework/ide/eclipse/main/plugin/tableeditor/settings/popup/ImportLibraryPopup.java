package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import org.eclipse.jface.dialogs.InputLoadingFormComposite;
import org.eclipse.jface.dialogs.RobotPopupDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

public class ImportLibraryPopup extends RobotPopupDialog {

    private InputLoadingFormComposite composite;
    private final RobotEditorCommandsStack commandsStack;
    private final RobotSuiteFile fileModel;

    public ImportLibraryPopup(final Shell parent, final RobotEditorCommandsStack commandsStack,
            final RobotSuiteFile fileModel) {
        super(parent);
        this.commandsStack = commandsStack;
        this.fileModel = fileModel;
    }

    @Override
    protected Control createDialogControls(final Composite parent) {
        composite = new ImportLibraryComposite(parent, commandsStack, fileModel);
        return composite;
    }

    @Override
    protected Control getFocusControl() {
        return composite.getFocusControl();
    }
}
