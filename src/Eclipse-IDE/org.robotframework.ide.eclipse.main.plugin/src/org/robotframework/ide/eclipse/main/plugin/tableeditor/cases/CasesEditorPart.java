package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesEditorPart.CasesEditor;

import com.google.common.base.Optional;

public class CasesEditorPart extends DISectionEditorPart<CasesEditor> {

    public CasesEditorPart() {
        super(CasesEditor.class);
        setTitleImage(RobotImages.getRobotImage().createImage());
    }

    public static class CasesEditor extends SectionEditorPart {

        private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.cases.context";

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
            // TODO Auto-generated method stub
        }

        @Override
        public Optional<RobotElement> provideSection(final RobotSuiteFile suite) {
            return suite.findSection(RobotCasesSection.class);
        }

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            return newArrayList();
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
