/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorSite;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement.OpenStrategy;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.OpenDeclarationHandler.E4OpenDeclarationHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class OpenDeclarationHandler extends DIParameterizedHandler<E4OpenDeclarationHandler> {

    public OpenDeclarationHandler() {
        super(E4OpenDeclarationHandler.class);
    }

    public static class E4OpenDeclarationHandler {
        @Inject
        private IEditorSite site;

        @Execute
        public Object openDeclaration(@Named(Selections.SELECTION) final IStructuredSelection selection,
                @Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile fileModel) {
            final RobotKeywordCall keywordCall = Selections.getSingleElement(selection, RobotKeywordCall.class);
            final String keywordName = keywordCall.getName();

            OpenStrategy openingStrategy = getOpeningStrategy(fileModel, keywordName);
            if (openingStrategy != null) {
                openingStrategy.run();
                return null;
            }

            final IPath fileParentsPath = fileModel.getFile().getParent().getFullPath();
            final IWorkspaceRoot wsRoot = fileModel.getProject().getProject().getWorkspace().getRoot();

            final List<IPath> resourcesPaths = fileModel.getResourcesPaths();
            for (final IPath resourcePath : resourcesPaths) {
                // if (resourcePath.isParameterized())
                if (resourcePath.isAbsolute()) {
                    // not good, we would have to create a symlink... maybe
                    // throw some kind of exception?
                } else {
                    final IPath currentPath = fileParentsPath.append(resourcePath);
                    final IFile potentialTarget = wsRoot.getFile(currentPath);
                    if (potentialTarget.exists()) {
                        final RobotSuiteFile potentialModel = RedPlugin.getModelManager().createSuiteFile(
                                potentialTarget);
                        openingStrategy = getOpeningStrategy(potentialModel, keywordName);
                        if (openingStrategy != null) {
                            openingStrategy.run();
                            return null;
                        }
                    }
                }
            }

            // TODO : look through imported libraries

            MessageDialog.openError(site.getShell(), "Unable to find declaration", "Unable to locate declaration of '"
                    + keywordName + "' keyword");
            return null;
        }

        private OpenStrategy getOpeningStrategy(final RobotSuiteFile file, final String keywordName) {
            final List<RobotKeywordDefinition> definedKeywords = file.getUserDefinedKeywords();
            for (final RobotKeywordDefinition def : definedKeywords) {
                if (def.getName().equals(keywordName)) {
                    return def.getOpenRobotEditorStrategy(site.getPage());
                }
            }
            return null;
        }
    }
}
