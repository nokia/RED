package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;

import com.google.common.base.Optional;

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
        @SuppressWarnings("unchecked")
        final Optional<RobotKeywordDefinition> definition = (Optional<RobotKeywordDefinition>) inputElement;
        final Map<String, RobotElement> keywordSettings = KeywordSettingsModel
                .findKeywordSettingsMapping(definition.orNull());
        return keywordSettings.entrySet().toArray();
    }

}
