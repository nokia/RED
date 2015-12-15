/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.general;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.e4.tools.compat.parts.DIEditorPart;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorPage;
import org.robotframework.ide.eclipse.main.plugin.project.editor.general.GeneralProjectConfigurationEditorPart.GeneralProjectConfigurationEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.viewers.ViewersCombiningSelectionProvider;

public class GeneralProjectConfigurationEditorPart extends DIEditorPart<GeneralProjectConfigurationEditor> {

    public GeneralProjectConfigurationEditorPart() {
        super(GeneralProjectConfigurationEditor.class);
    }

    static class GeneralProjectConfigurationEditor extends RedProjectEditorPage {

        private FrameworksSectionFormFragment frameworksFragment;
        private VariableMappingsFormFragment variableMappingsFragment;

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            frameworksFragment = new FrameworksSectionFormFragment();
            variableMappingsFragment = new VariableMappingsFormFragment();
            return newArrayList(frameworksFragment, variableMappingsFragment);
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            return new ViewersCombiningSelectionProvider(frameworksFragment.getViewer(),
                    variableMappingsFragment.getViewer());
        }
    }
}
