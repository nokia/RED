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
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.ReloadPythonModulesHandler.E4ReloadPythonModulesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;


public class ReloadPythonModulesHandler extends DIParameterizedHandler<E4ReloadPythonModulesHandler> {

    public ReloadPythonModulesHandler() {
        super(E4ReloadPythonModulesHandler.class);
    }

    public static class E4ReloadPythonModulesHandler {

        @Execute
        public Object reloadProject(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final List<IProject> projects = Selections.getElements(selection, IProject.class);

            final Set<RobotRuntimeEnvironment> envsToReset = new HashSet<>();
            for (final IProject project : projects) {
                final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
                envsToReset.add(robotProject.getRuntimeEnvironment());

                robotProject.clearCachedData();
            }
            for (final RobotRuntimeEnvironment environment : envsToReset) {
                environment.resetCommandExecutors();
            }

            reparseModelsInOpenedEditors();

            return null;
        }

        private void reparseModelsInOpenedEditors() {
            // this ensures that models which are currently opened are reparsed in order to
            // recreate removed cached data
            final List<IEditorReference> editors = new ArrayList<IEditorReference>();
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
