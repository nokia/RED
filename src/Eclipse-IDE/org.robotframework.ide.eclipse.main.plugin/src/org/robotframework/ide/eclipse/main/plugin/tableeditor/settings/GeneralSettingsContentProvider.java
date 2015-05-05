package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

class GeneralSettingsContentProvider implements IStructuredContentProvider {

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        // nothing to do
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        if (inputElement instanceof GeneralSettingsModel) {
            final GeneralSettingsModel model = (GeneralSettingsModel) inputElement;
            return model.getEntries().toArray();

        }
        return new Object[0];
    }

}
