package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.jface.dialogs.InputLoadingFormComposite;
import org.robotframework.red.jface.dialogs.RobotPopupDialog;

class LibraryDocumentationPopup extends RobotPopupDialog {

    private InputLoadingFormComposite composite;
    private final LibrarySpecification specification;

    LibraryDocumentationPopup(final Shell parent, final LibrarySpecification spec) {
        super(parent);
        this.specification = spec;
    }

    @Override
    protected Control createDialogControls(final Composite parent) {
        composite = new LibraryDocumentationComposite(parent, specification);
        return composite;
    }

    @Override
    protected Control getFocusControl() {
        return composite.getFocusControl();
    }
}
