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
import org.robotframework.red.viewers.ViewersCombiningSelectionProvider;

public class VariablesProjectConfigurationEditorPart extends DIEditorPart<VariablesProjectConfigurationEditor> {

    public VariablesProjectConfigurationEditorPart() {
        super(VariablesProjectConfigurationEditor.class);
    }

    static class VariablesProjectConfigurationEditor extends RedProjectEditorPage {

        private VariableMappingsFormFragment variableMappingsFragment;

        private VariableFilesFormFragment variablesFragment;

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            variableMappingsFragment = new VariableMappingsFormFragment();
            variablesFragment = new VariableFilesFormFragment();
            return newArrayList(variableMappingsFragment, variablesFragment);
        }

        @Override
        protected int getNumberOfColumnsInForm() {
            return 2;
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            return new ViewersCombiningSelectionProvider(variableMappingsFragment.getViewer(),
                    variablesFragment.getViewer());
        }
    }
}
