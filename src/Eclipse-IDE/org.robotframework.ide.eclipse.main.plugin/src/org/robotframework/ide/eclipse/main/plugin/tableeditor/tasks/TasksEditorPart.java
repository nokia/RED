/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.ITableHyperlinksDetector;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TreeLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.TasksEditorPart.TasksEditor;
import org.robotframework.red.graphics.ImagesManager;

public class TasksEditorPart extends DISectionEditorPart<TasksEditor> {

    public TasksEditorPart() {
        super(TasksEditor.class);
        setTitleImage(ImagesManager.getImage(RedImages.getRobotImage()));
    }

    public static class TasksEditor extends SectionEditorPart {

        private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.tasks.context";

        private TasksFormFragment tasksFragment;

        @Override
        protected String getContextId() {
            return CONTEXT_ID;
        }

        @Override
        public String getId() {
            return "red.tasks";
        }

        @Override
        protected String getTitle() {
            return "Tasks";
        }

        @Override
        protected String getSectionName() {
            return RobotTasksSection.SECTION_NAME;
        }

        @Override
        public boolean isPartFor(final RobotSuiteFileSection section) {
            return section instanceof RobotTasksSection;
        }

        @Override
        public void revealElement(final RobotElement robotElement) {
            tasksFragment.revealElement(robotElement, false);
        }

        @Override
        public void revealElementAndFocus(final RobotElement robotElement) {
            tasksFragment.revealElement(robotElement, true);
        }

        @Override
        public Optional<? extends RobotSuiteFileSection> provideSection(final RobotSuiteFile suite) {
            return suite.findSection(RobotTasksSection.class);
        }

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            tasksFragment = new TasksFormFragment();
            return newArrayList(tasksFragment);
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            return tasksFragment.getSelectionProvider();
        }

        @Override
        public SelectionLayerAccessor getSelectionLayerAccessor() {
            return tasksFragment.getSelectionLayerAccessor();
        }
        
        @Override
        public Optional<TreeLayerAccessor> getTreeLayerAccessor() {
            return Optional.of(tasksFragment.getTreeLayerAccessor());
        }

        @Override
        public void aboutToChangeToOtherPage() {
            tasksFragment.aboutToChangeToOtherPage();
        }
        
        @Override
        public void waitForPendingJobs() {
            return;
        }

        @Override
        public List<ITableHyperlinksDetector> getDetectors() {
            return tasksFragment.getDetectors();
        }
    }
}
