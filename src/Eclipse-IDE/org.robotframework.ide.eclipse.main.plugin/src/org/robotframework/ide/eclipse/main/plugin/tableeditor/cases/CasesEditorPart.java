package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesEditorPart.CasesEditor;

import com.google.common.base.Optional;

public class CasesEditorPart extends DISectionEditorPart<CasesEditor> {

    public CasesEditorPart() {
        super(CasesEditor.class);
        setTitleImage(RobotImages.getRobotImage().createImage());
    }

    public static class CasesEditor extends SectionEditorPart {

        private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.cases.context";
        private CasesEditorFormFragment casesFragment;

        @Override
        protected String getContextId() {
            return CONTEXT_ID;
        }

        @Override
        protected String getTitle() {
            return "Test Cases";
        }

        @Override
        protected String getSectionName() {
            return RobotCasesSection.SECTION_NAME;
        }

        @Override
        public boolean isPartFor(final RobotSuiteFileSection section) {
            return section instanceof RobotCasesSection;
        }

        @Override
        public void revealElement(final RobotElement robotElement) {
            casesFragment.revealCase((RobotCase) robotElement);
        }

        @Override
        public Optional<RobotElement> provideSection(final RobotSuiteFile suite) {
            return suite.findSection(RobotCasesSection.class);
        }

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            casesFragment = new CasesEditorFormFragment();
            return newArrayList(casesFragment);
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            return casesFragment.getViewer();
        }

        @Override
        protected FocusedViewerAccessor getActiveCellAccessor() {
            return new FocusedViewerAccessor(casesFragment.getViewer());
        }
    }
}
