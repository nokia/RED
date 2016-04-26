/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ExcludedFolderPath;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;

import com.google.common.base.Optional;

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
            return Optional.absent();
        }
        final CompositeChange change = new CompositeChange(
                redXmlFile.getName() + " - " + redXmlFile.getParent().getFullPath().toString());

        change.add(collectChangesInExcludedPaths(currentlyEditedConfig));
        return Optional.of(Changes.normalizeCompositeChange(change));
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

    private Change collectChangesInExcludedPaths(final RobotProjectConfig config) {
        final CompositeChange change = new CompositeChange(
                "'" + redXmlFile.getFullPath() + "': paths excluded from validation");

        for (final ExcludedFolderPath excluded : config.getExcludedPath()) {

            final IPath potentiallyAffectedPath = excluded.asPath();
            final IPath adjustedPathBeforeRefactoring = Changes
                    .excapeXmlCharacters(pathBeforeRefactoring.removeFirstSegments(1));
            if (pathAfterRefactoring.isPresent()) {
                final IPath adjustedPathAfterRefactoring = Changes
                        .excapeXmlCharacters(pathAfterRefactoring.get().removeFirstSegments(1));

                final Optional<IPath> transformedPath = Changes.transformAffectedPath(adjustedPathBeforeRefactoring,
                        adjustedPathAfterRefactoring, potentiallyAffectedPath);
                if (transformedPath.isPresent()) {
                    change.add(new ExcludedPathModifyChange(redXmlFile, excluded, transformedPath.get()));
                }
            } else if (adjustedPathBeforeRefactoring.isPrefixOf(potentiallyAffectedPath)) {
                change.add(new ExcludedPathRemoveChange(redXmlFile, config, excluded));
            }
        }
        return Changes.normalizeCompositeChange(change);
    }

}
