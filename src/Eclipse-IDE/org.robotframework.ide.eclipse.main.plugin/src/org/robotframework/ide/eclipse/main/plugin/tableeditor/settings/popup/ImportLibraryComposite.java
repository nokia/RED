package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import org.eclipse.jface.dialogs.InputLoadingFormComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

public class ImportLibraryComposite extends InputLoadingFormComposite<Tree> {

    public ImportLibraryComposite(final Composite parent, final String title) {
        super(parent, SWT.NONE, title);
    }

    @Override
    protected Tree createControl(final Composite parent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void createActions() {
        // nothing to do
    }

    @Override
    protected InputLoadingFormComposite.InputJob provideInputCollectingJob() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void fillControl(final Object jobResult) {
        // TODO Auto-generated method stub

    }

}
