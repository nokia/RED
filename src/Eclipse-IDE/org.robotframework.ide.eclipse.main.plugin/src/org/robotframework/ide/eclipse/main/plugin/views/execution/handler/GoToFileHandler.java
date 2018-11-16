/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import java.io.File;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.DefinitionPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.ElementOpenMode;
import org.robotframework.ide.eclipse.main.plugin.model.locators.CasesDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.CasesDefinitionLocator.CaseDetector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.GoToFileHandler.E4GoToFileHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.annotations.VisibleForTesting;

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
            openExecutionNodeSourceFile(node, (file, caseName) -> new CasesDefinitionLocator(file)
                    .locateCaseDefinition(createDetector(caseName, E4GoToFileHandler::selectElement)));
        }

        @VisibleForTesting
        static void openExecutionNodeSourceFile(final ExecutionTreeNode node,
                final BiConsumer<IFile, String> caseSelectionConsumer) {
            if (node == null || node.getPath() == null) {
                return;
            }
            final IPath sourcePath = new Path(new File(node.getPath()).getAbsolutePath());
            final Optional<IFile> sourceFile = new RedWorkspace().fileForUri(sourcePath.toFile().toURI());
            sourceFile.ifPresent(file -> caseSelectionConsumer.accept(file, node.getName()));
        }

        private static void selectElement(final RobotFileInternalElement element) {
            final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            element.getOpenRobotEditorStrategy().run(page, ElementOpenMode.OPEN_IN_SOURCE);

            final ISelectionProvider selectionProvider = page.getActiveEditor().getSite().getSelectionProvider();
            if (selectionProvider != null) {
                final DefinitionPosition position = element.getDefinitionPosition();
                selectionProvider.setSelection(new TextSelection(position.getOffset(), position.getLength()));
            }
        }

        private static CaseDetector createDetector(final String caseName,
                final Consumer<RobotFileInternalElement> elementSelector) {
            return (file, taskOrTest) -> {
                if (taskOrTest.getName().equalsIgnoreCase(caseName)) {
                    elementSelector.accept(taskOrTest);
                    return ContinueDecision.STOP;
                } else {
                    return ContinueDecision.CONTINUE;
                }
            };
        }
    }
}
