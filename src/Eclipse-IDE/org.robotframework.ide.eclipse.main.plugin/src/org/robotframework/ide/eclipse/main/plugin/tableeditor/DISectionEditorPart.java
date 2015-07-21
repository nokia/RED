package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.e4.tools.compat.parts.DIEditorPart;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;

import com.google.common.base.Optional;

public class DISectionEditorPart<C extends ISectionEditorPart> extends DIEditorPart<C> implements ISectionEditorPart {

    public DISectionEditorPart(final Class<C> clazz) {
        super(clazz);
    }

    @Override
    public void updateOnActivation() {
        getComponent().updateOnActivation();
    }

    @Override
    public Optional<RobotElement> provideSection(final RobotSuiteFile suiteModel) {
        return getComponent().provideSection(suiteModel);
    }

    @Override
    public boolean isPartFor(final RobotSuiteFileSection section) {
        return getComponent().isPartFor(section);
    }

    @Override
    public void revealElement(final RobotElement robotElement) {
        getComponent().revealElement(robotElement);
    }

    @Override
    public void setFocus() {
        getComponent().setFocus();
    }

    @Override
    public FocusedViewerAccessor getFocusedViewerAccessor() {
        return getComponent().getFocusedViewerAccessor();
    }
}
