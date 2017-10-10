/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedFolderPath;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;

/**
 * @author Michal Anglart
 */
class RedXmlInProjectEditorChangesCollector {

    private final IFile redXmlFile;

    private final IPath pathBeforeRefactoring;

    private final Optional<IPath> pathAfterRefactoring;

    RedXmlInProjectEditorChangesCollector(final IFile redXmlFile, final IPath pathBeforeRefactoring,
            final Optional<IPath> pathAfterRefactoring) {
        this.redXmlFile = redXmlFile;
        this.pathBeforeRefactoring = pathBeforeRefactoring;
        this.pathAfterRefactoring = pathAfterRefactoring;
    }

    Optional<Change> collect() {
        final RobotProjectConfig currentlyEditedConfig = getConfigUnderEdit(redXmlFile);
        if (currentlyEditedConfig == null) {
            return Optional.empty();
        }
        return collectChanges(currentlyEditedConfig);
    }

    private RobotProjectConfig getConfigUnderEdit(final IFile redXmlFile) {
        return SwtThread.syncEval(new Evaluation<RobotProjectConfig>() {

            @Override
            public RobotProjectConfig runCalculation() {
                final IEditorPart editor = findEditor(new FileEditorInput(redXmlFile));

                if (editor instanceof RedProjectEditor) {
                    final RedProjectEditorInput input = ((RedProjectEditor) editor).getRedProjectEditorInput();
                    return input.getProjectConfiguration();
                }
                return null;
            }
        });
    }

    private static IEditorPart findEditor(final FileEditorInput input) {
        final IEditorReference[] editors = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow()
                .getActivePage()
                .findEditors(input, RedProjectEditor.ID, IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
        return editors.length > 0 ? editors[0].getEditor(true) : null;
    }

    private Optional<Change> collectChanges(final RobotProjectConfig currentlyEditedConfig) {
        final CompositeChange change = new CompositeChange(
                redXmlFile.getName() + " - " + redXmlFile.getParent().getFullPath().toString());

        final CompositeChange excludedPathsChanges = new CompositeChange(
                "'" + redXmlFile.getFullPath() + "': paths excluded from validation");
        new ExcludedPathsChangesDetector(pathBeforeRefactoring, pathAfterRefactoring, currentlyEditedConfig)
                .detect(new OpenedConfigExcludedPathsProcessor(excludedPathsChanges));
        change.add(Changes.normalizeCompositeChange(excludedPathsChanges));

        final CompositeChange librariesPathsChanges = new CompositeChange(
                "'" + redXmlFile.getFullPath() + "': referenced libraries");
        new LibrariesChangesDetector(pathBeforeRefactoring, pathAfterRefactoring, currentlyEditedConfig)
                .detect(new OpenedConfigReferencedLibrariesPathsProcessor(librariesPathsChanges));
        change.add(Changes.normalizeCompositeChange(librariesPathsChanges));

        return Optional.of(Changes.normalizeCompositeChange(change));
    }

    private class OpenedConfigExcludedPathsProcessor implements RedXmlChangesProcessor<ExcludedFolderPath> {

        private final CompositeChange compositeChange;

        public OpenedConfigExcludedPathsProcessor(final CompositeChange compositeChange) {
            this.compositeChange = compositeChange;
        }

        @Override
        public void pathModified(final ExcludedFolderPath excludedPath, final ExcludedFolderPath newPath) {
            compositeChange.add(new ExcludedPathModifyChange(redXmlFile, excludedPath, newPath));
        }

        @Override
        public void pathRemoved(final RobotProjectConfig config, final ExcludedFolderPath pathToRemove) {
            compositeChange.add(new ExcludedPathRemoveChange(redXmlFile, config, pathToRemove));
        }
    }

    private class OpenedConfigReferencedLibrariesPathsProcessor implements RedXmlChangesProcessor<ReferencedLibrary> {

        private final CompositeChange compositeChange;

        public OpenedConfigReferencedLibrariesPathsProcessor(final CompositeChange compositeChange) {
            this.compositeChange = compositeChange;
        }

        @Override
        public void pathModified(final ReferencedLibrary library, final ReferencedLibrary newLibrary) {
            compositeChange.add(new LibraryModifyChange(redXmlFile, library, newLibrary));
        }

        @Override
        public void pathRemoved(final RobotProjectConfig config, final ReferencedLibrary library) {
            compositeChange.add(new LibraryRemoveChange(redXmlFile, config, library));
        }
    }
}
