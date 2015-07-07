package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariablesEditorPart.VariablesEditor;

import com.google.common.base.Optional;

public class VariablesEditorPart extends DISectionEditorPart<VariablesEditor> {

    public VariablesEditorPart() {
        super(VariablesEditor.class);
        setTitleImage(RobotImages.getRobotVariableImage().createImage());
    }

    public static class VariablesEditor extends SectionEditorPart {

        private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.variables.context";

        private VariablesEditorFormFragment variablesFragment;

        @Override
        protected String getContextId() {
            return CONTEXT_ID;
        }

        @Override
        protected String getTitle() {
            return "Variables";
        }

        @Override
        protected String getSectionName() {
            return RobotVariablesSection.SECTION_NAME;
        }

        @Override
        public boolean isPartFor(final RobotSuiteFileSection section) {
            return section instanceof RobotVariablesSection;
        }

        @Override
        public void revealElement(final RobotElement robotElement) {
            variablesFragment.revealVariable((RobotVariable) robotElement);
        }

        @Override
        public Optional<RobotElement> provideSection(final RobotSuiteFile suite) {
            return suite.findSection(RobotVariablesSection.class);
        }

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            variablesFragment = new VariablesEditorFormFragment();
            return newArrayList(variablesFragment);
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            return variablesFragment.getViewer();
        }

        @Override
        protected FocusedViewerAccessor getActiveCellAccessor() {
            return new FocusedViewerAccessor(variablesFragment.getViewer());
        }
    }
}
