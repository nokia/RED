/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.e4.tools.compat.parts.DIEditorPart;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorPage;
import org.robotframework.ide.eclipse.main.plugin.project.editor.variables.VariablesProjectConfigurationEditorPart.VariablesProjectConfigurationEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;

public class VariablesProjectConfigurationEditorPart extends DIEditorPart<VariablesProjectConfigurationEditor> {

    public VariablesProjectConfigurationEditorPart() {
        super(VariablesProjectConfigurationEditor.class);
    }

    static class VariablesProjectConfigurationEditor extends RedProjectEditorPage {

        private VariableFilesFormFragment variablesFragment;

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            variablesFragment = new VariableFilesFormFragment();
            return newArrayList(variablesFragment);
        }

        @Override
        protected int getNumberOfColumnsInForm() {
            return 1;
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            return variablesFragment.getViewer();
        }
    }
}
