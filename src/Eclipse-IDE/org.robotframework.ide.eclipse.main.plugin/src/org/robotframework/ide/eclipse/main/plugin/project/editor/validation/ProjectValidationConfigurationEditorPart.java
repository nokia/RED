/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.validation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.e4.tools.compat.parts.DIEditorPart;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorPage;
import org.robotframework.ide.eclipse.main.plugin.project.editor.validation.ProjectValidationConfigurationEditorPart.ProjectValidationConfigurationEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;

/**
 * @author Michal Anglart
 *
 */
public class ProjectValidationConfigurationEditorPart extends DIEditorPart<ProjectValidationConfigurationEditor> {

    public ProjectValidationConfigurationEditorPart() {
        super(ProjectValidationConfigurationEditor.class);
    }

    static class ProjectValidationConfigurationEditor extends RedProjectEditorPage {

        private ProjectValidationFormFragment projectValidationFormFragment;

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            projectValidationFormFragment = new ProjectValidationFormFragment();
            return newArrayList(projectValidationFormFragment);
        }

        @Override
        protected int getNumberOfColumnsInForm() {
            return 1;
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            return projectValidationFormFragment.getViewer();
        }
    }

}
