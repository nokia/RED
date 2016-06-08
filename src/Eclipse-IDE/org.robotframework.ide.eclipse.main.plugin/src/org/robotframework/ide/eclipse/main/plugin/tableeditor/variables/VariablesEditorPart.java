/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariablesEditorPart.VariablesEditor;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;

public class VariablesEditorPart extends DISectionEditorPart<VariablesEditor> {

    public VariablesEditorPart() {
        super(VariablesEditor.class);
        setTitleImage(ImagesManager.getImage(RedImages.getRobotVariableImage()));
    }

    public static class VariablesEditor extends SectionEditorPart {

        private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.variables.context";

        private VariablesEditorFormFragment variablesFragment;

        @Override
        protected String getContextId() {
            return CONTEXT_ID;
        }

        @Override
        public String getId() {
            return "red.variables";
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
            if (robotElement instanceof RobotVariable) {
                variablesFragment.revealVariable((RobotVariable) robotElement);
            }
        }

        @Override
        public Optional<? extends RobotSuiteFileSection> provideSection(final RobotSuiteFile suite) {
            return suite.findSection(RobotVariablesSection.class);
        }

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            variablesFragment = new VariablesEditorFormFragment();
            return newArrayList(variablesFragment);
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            return variablesFragment.getSelectionProvider();
        }

        @Override
        public FocusedViewerAccessor getFocusedViewerAccessor() {
            return null;
        }

        @Override
        public SelectionLayerAccessor getSelectionLayerAccessor() {
            return variablesFragment.getSelectionLayerAccessor();
        }
    }
}
