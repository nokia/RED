/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.ResetRobotEnvironmentHandler.E4ResetRobotEnvironmentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;


public class ResetRobotEnvironmentHandler extends DIParameterizedHandler<E4ResetRobotEnvironmentHandler> {

    public ResetRobotEnvironmentHandler() {
        super(E4ResetRobotEnvironmentHandler.class);
    }

    public static class E4ResetRobotEnvironmentHandler {

        @Execute
        public void reloadProject(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final List<IProject> projects = Selections.getAdaptableElements(selection, IProject.class);

            final Set<RobotRuntimeEnvironment> envsToReset = new HashSet<>();
            for (final IProject project : projects) {
                final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
                if (robotProject.getRuntimeEnvironment() != null) {
                    envsToReset.add(robotProject.getRuntimeEnvironment());
                }

                robotProject.clearCachedData();
            }
            for (final RobotRuntimeEnvironment environment : envsToReset) {
                environment.resetCommandExecutors();
            }

            reparseModelsInOpenedEditors();
        }

        private void reparseModelsInOpenedEditors() {
            // this ensures that models which are currently opened are reparsed in order to
            // recreate removed cached data
            final List<IEditorReference> editors = new ArrayList<>();
            for (final IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                for (final IWorkbenchPage page : window.getPages()) {
                    for (final IEditorReference editor : page.getEditorReferences()) {
                        if (RobotFormEditor.ID.equals(editor.getId())) {
                            editors.add(editor);
                        }
                    }
                }
            }
            for (final IEditorReference editorReference : editors) {
                final IEditorPart editor = editorReference.getEditor(false);
                if (editor != null) {
                    final RobotFormEditor robotEditor = (RobotFormEditor) editor;
                    final RobotSuiteFile model = robotEditor.provideSuiteModel();
                    final String fileContent = robotEditor.getSourceEditor().getDocument().get();
                    model.reparseEverything(fileContent);
                }
            }
        }
    }
}
