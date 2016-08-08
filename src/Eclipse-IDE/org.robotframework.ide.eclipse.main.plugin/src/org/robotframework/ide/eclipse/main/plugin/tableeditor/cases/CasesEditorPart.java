/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TreeLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesEditorPart.CasesEditor;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;

public class CasesEditorPart extends DISectionEditorPart<CasesEditor> {

    public CasesEditorPart() {
        super(CasesEditor.class);
        setTitleImage(ImagesManager.getImage(RedImages.getRobotImage()));
    }

    public static class CasesEditor extends SectionEditorPart {

        private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.cases.context";

        private CasesEditorFormFragment casesFragment;

        @Override
        protected String getContextId() {
            return CONTEXT_ID;
        }

        @Override
        public String getId() {
            return "red.cases";
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
            casesFragment.revealElement(robotElement);
        }

        @Override
        public Optional<? extends RobotSuiteFileSection> provideSection(final RobotSuiteFile suite) {
            return suite.findSection(RobotCasesSection.class);
        }

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            casesFragment = new CasesEditorFormFragment();
            return newArrayList(casesFragment);
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            return casesFragment.getSelectionProvider();
        }

        @Override
        public SelectionLayerAccessor getSelectionLayerAccessor() {
            return casesFragment.getSelectionLayerAccessor();
        }
        
        @Override
        public Optional<TreeLayerAccessor> getTreeLayerAccessor() {
            return Optional.of(casesFragment.getTreeLayerAccessor());
        }

        @Override
        public void aboutToChangeToOtherPage() {
            casesFragment.aboutToChangeToOtherPage();
        }
        
        @Override
        public void waitForPendingJobs() {
            return;
        }
    }
}
