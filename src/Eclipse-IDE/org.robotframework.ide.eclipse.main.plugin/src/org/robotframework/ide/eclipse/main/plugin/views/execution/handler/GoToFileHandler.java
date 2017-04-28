/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import java.io.File;

import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.DefinitionPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.ElementOpenMode;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.TestCasesDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.TestCasesDefinitionLocator.TestCaseDetector;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.GoToFileHandler.E4GoToFileHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;


public class GoToFileHandler extends DIParameterizedHandler<E4GoToFileHandler> {

    public GoToFileHandler() {
        super(E4GoToFileHandler.class);
    }

    public static class E4GoToFileHandler {

        @Execute
        public void goToFile(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            openExecutionNodeSourceFile(Selections.getSingleElement(selection, ExecutionTreeNode.class));
        }

        public static void openExecutionNodeSourceFile(final ExecutionTreeNode node) {
            if (node == null || node.getPath() == null) {
                return;
            }
            final IPath sourcePath = new Path(new File(node.getPath()).getAbsolutePath());
            final IFile sourceFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(sourcePath);
            if (sourceFile == null || !sourceFile.exists()) {
                return;
            }
            new TestCasesDefinitionLocator(sourceFile).locateTestCaseDefinition(createDetector(node.getName()));
        }

        private static TestCaseDetector createDetector(final String caseName) {
            return new TestCaseDetector() {

                @Override
                public ContinueDecision testCaseDetected(final RobotSuiteFile file, final RobotCase testCase) {
                    if (testCase.getName().equalsIgnoreCase(caseName)) {
                        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        testCase.getOpenRobotEditorStrategy().run(page, ElementOpenMode.OPEN_IN_SOURCE);

                        final ISelectionProvider selectionProvider = page.getActiveEditor()
                                .getSite()
                                .getSelectionProvider();
                        if (selectionProvider != null) {
                            final DefinitionPosition position = testCase.getDefinitionPosition();
                            selectionProvider
                                    .setSelection(new TextSelection(position.getOffset(), position.getLength()));
                        }
                        return ContinueDecision.STOP;
                    } else {
                        return ContinueDecision.CONTINUE;
                    }
                }
            };
        }
    }
}
