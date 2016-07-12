/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.KeywordsEditorPart.KeywordsEditor;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;

public class KeywordsEditorPart extends DISectionEditorPart<KeywordsEditor> {

    public KeywordsEditorPart() {
        super(KeywordsEditor.class);
        setTitleImage(ImagesManager.getImage(RedImages.getUserKeywordImage()));
    }

    public static class KeywordsEditor extends SectionEditorPart {

        private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.keywords.context";

        private org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable.KeywordsEditorFormFragment keywordsFragment;
        private KeywordSettingsFormFragment detailsFragment;

        @Override
        protected String getContextId() {
            return CONTEXT_ID;
        }

        @Override
        public String getId() {
            return "red.keywords";
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
        public Optional<? extends RobotSuiteFileSection> provideSection(final RobotSuiteFile suite) {
            return suite.findSection(RobotKeywordsSection.class);
        }

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            keywordsFragment = new org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable.KeywordsEditorFormFragment();
//            org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable.KeywordsEditorFormFragment k2 = 
//                    new org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable.KeywordsEditorFormFragment();
            detailsFragment = new KeywordSettingsFormFragment();
            return newArrayList(keywordsFragment, detailsFragment);
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            //return keywordsFragment.getViewer();
            return keywordsFragment.getSelectionProvider();
        }

        @Override
        public FocusedViewerAccessor getFocusedViewerAccessor() {
            //return keywordsFragment.getFocusedViewerAccessor();
            return null;
        }

        @Override
        public SelectionLayerAccessor getSelectionLayerAccessor() {
            return keywordsFragment.getSelectionLayerAccessor();
        }

        @Override
        public void waitForPendingJobs() {
            return;
        }
    }
}
