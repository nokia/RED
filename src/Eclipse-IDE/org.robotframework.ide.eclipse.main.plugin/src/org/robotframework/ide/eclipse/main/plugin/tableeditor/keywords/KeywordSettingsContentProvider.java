package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class KeywordSettingsContentProvider implements IStructuredContentProvider {

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
        final KeywordSettingsModel model = (KeywordSettingsModel) inputElement;
        return model.getEntries().toArray();
    }

}
