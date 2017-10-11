/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedFolderPath;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfigReader.RobotProjectConfigWithLines;
import org.rf.ide.core.testdata.model.FileRegion;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.refactoring.RedXmlInFileChangesCollector.TextBasedChangesProcessor;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;

import com.google.common.base.Charsets;

/**
 * @author Michal Anglart
 */
class RedXmlInTextEditorChangesCollector {

    private final IFile redXmlFile;

    private final Optional<IPath> pathAfterRefactoring;

    private final IPath pathBeforeRefactoring;

    RedXmlInTextEditorChangesCollector(final IFile redXmlFile, final IPath pathBeforeRefactoring,
            final Optional<IPath> pathAfterRefactoring) {
        this.redXmlFile = redXmlFile;
        this.pathBeforeRefactoring = pathBeforeRefactoring;
        this.pathAfterRefactoring = pathAfterRefactoring;
    }

    Optional<Change> collect() {
        final IDocument currentlyEditedConfigDocument = getDocumentUnderEdit(redXmlFile);
        if (currentlyEditedConfigDocument == null) {
            return Optional.empty();
        }
        return collectChanges(currentlyEditedConfigDocument);
    }

    private IDocument getDocumentUnderEdit(final IFile redXmlFile) {
        return SwtThread.syncEval(new Evaluation<IDocument>() {

            @Override
            public IDocument runCalculation() {
                final FileEditorInput input = new FileEditorInput(redXmlFile);
                final IEditorPart editor = findEditor(input);

                if (editor instanceof ITextEditor) {
                    final ITextEditor ed = ((ITextEditor) editor);
                    return ed.getDocumentProvider().getDocument(input);
                }
                return null;
            }
        });
    }

    private static IEditorPart findEditor(final FileEditorInput input) {
        final IEditorReference[] editors = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow()
                .getActivePage()
                .findEditors(input, EditorsUI.DEFAULT_TEXT_EDITOR_ID,
                        IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
        return editors.length > 0 ? editors[0].getEditor(true) : null;
    }

    private Optional<Change> collectChanges(final IDocument document) {
        final RobotProjectConfigWithLines configWithLines = new RedEclipseProjectConfigReader()
                .readConfigurationWithLines(new ByteArrayInputStream(document.get().getBytes(Charsets.UTF_8)));
        final RobotProjectConfig config = configWithLines.getConfigurationModel();
        final Function<FileRegion, FileRegion> regionMapper = r -> TextOperations.getAffectedRegion(r, document);

        final TextBasedChangesProcessor<ExcludedFolderPath> pathsProcessor = new TextBasedChangesProcessor<>(
                configWithLines, regionMapper);
        new ExcludedPathsChangesDetector(pathBeforeRefactoring, pathAfterRefactoring, config).detect(pathsProcessor);

        final TextBasedChangesProcessor<ReferencedLibrary> libsProcessor = new TextBasedChangesProcessor<>(
                configWithLines, regionMapper);
        new LibrariesChangesDetector(pathBeforeRefactoring, pathAfterRefactoring, config).detect(libsProcessor);

        if (Stream.of(pathsProcessor, libsProcessor).anyMatch(TextBasedChangesProcessor::hasEditsCollected)) {
            final TextEdit[] excludedPathsEdits = pathsProcessor.getEdits();
            final TextEdit[] librariesEdits = libsProcessor.getEdits();

            final MultiTextEdit multiTextEdit = new MultiTextEdit();
            multiTextEdit.addChildren(excludedPathsEdits);
            multiTextEdit.addChildren(librariesEdits);

            final DocumentChange docChange = new DocumentChange(
                    redXmlFile.getName() + " - " + redXmlFile.getParent().getFullPath().toString(), document);
            docChange.setEdit(multiTextEdit);
            if (excludedPathsEdits.length > 0) {
                docChange.addTextEditGroup(
                        new TextEditGroup("Change paths excluded from validation", excludedPathsEdits));
            }
            if (librariesEdits.length > 0) {
                docChange.addTextEditGroup(new TextEditGroup("Change referenced libraries", librariesEdits));
            }
            return Optional.of(docChange);
        } else {
            return Optional.empty();
        }
    }
}
