package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import org.eclipse.jface.dialogs.InputLoadingFormComposite;
import org.eclipse.jface.dialogs.RobotPopupDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

public class ImportLibraryPopup extends RobotPopupDialog {

    private InputLoadingFormComposite<Tree> composite;

    public ImportLibraryPopup(final Shell parent) {
        super(parent);
    }

    @Override
    protected Control createDialogControls(final Composite parent) {
        composite = new ImportLibraryComposite(parent, "Import Library");
        return composite;
    }

    @Override
    protected Control getFocusControl() {
        return composite.getFocusControl();
    }
}
