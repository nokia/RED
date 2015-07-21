package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.KeywordsEditorPart.KeywordsEditor;

import com.google.common.base.Optional;

public class KeywordsEditorPart extends DISectionEditorPart<KeywordsEditor> {

    public KeywordsEditorPart() {
        super(KeywordsEditor.class);
        setTitleImage(RobotImages.getUserKeywordImage().createImage());
    }

    public static class KeywordsEditor extends SectionEditorPart {

        private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.keywords.context";
        private KeywordsEditorFormFragment keywordsFragment;

        @Override
        protected String getContextId() {
            return CONTEXT_ID;
        }

        @Override
        protected String getTitle() {
            return "User Keywords";
        }

        @Override
        protected String getSectionName() {
            return RobotKeywordsSection.SECTION_NAME;
        }

        @Override
        public boolean isPartFor(final RobotSuiteFileSection section) {
            return section instanceof RobotKeywordsSection;
        }

        @Override
        public void revealElement(final RobotElement robotElement) {
            keywordsFragment.revealElement(robotElement);
        }

        @Override
        public Optional<RobotElement> provideSection(final RobotSuiteFile suite) {
            return suite.findSection(RobotKeywordsSection.class);
        }

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            keywordsFragment = new KeywordsEditorFormFragment();
            return newArrayList(keywordsFragment);
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            return keywordsFragment.getViewer();
        }

        @Override
        public FocusedViewerAccessor getFocusedViewerAccessor() {
            return new FocusedViewerAccessor(keywordsFragment.getViewer());
        }
    }
}
