/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedFolderPath;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfigReader.RobotProjectConfigWithLines;
import org.rf.ide.core.project.RobotProjectConfigWriter;
import org.rf.ide.core.testdata.model.FileRegion;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigReader;

/**
 * @author Michal Anglart
 */
class RedXmlInFileChangesCollector {

    private final IFile redXmlFile;

    private final Optional<IPath> pathAfterRefactoring;

    private final IPath pathBeforeRefactoring;

    RedXmlInFileChangesCollector(final IFile redXmlFile, final IPath pathBeforeRefactoring,
            final Optional<IPath> pathAfterRefactoring) {
        this.redXmlFile = redXmlFile;
        this.pathBeforeRefactoring = pathBeforeRefactoring;
        this.pathAfterRefactoring = pathAfterRefactoring;
    }

    Optional<Change> collect() {
        final RobotProjectConfigWithLines configWithLines = new RedEclipseProjectConfigReader()
                .readConfigurationWithLines(redXmlFile);
        final RobotProjectConfig config = configWithLines.getConfigurationModel();
        final Function<FileRegion, FileRegion> regionMapper = r -> TextOperations.getAffectedRegion(r, redXmlFile);

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

            final TextFileChange fileChange = new TextFileChange(
                    redXmlFile.getName() + " - " + redXmlFile.getParent().getFullPath().toString(), redXmlFile);
            fileChange.setEdit(multiTextEdit);
            if (excludedPathsEdits.length > 0) {
                fileChange.addTextEditGroup(
                        new TextEditGroup("Change paths excluded from validation", excludedPathsEdits));
            }
            if (librariesEdits.length > 0) {
                fileChange.addTextEditGroup(new TextEditGroup("Change referenced libraries", librariesEdits));
            }
            return Optional.of(fileChange);
        } else {
            return Optional.empty();
        }
    }

    static class TextBasedChangesProcessor<T> implements RedXmlChangesProcessor<T> {

        private final RobotProjectConfigWithLines config;

        private final List<TextEdit> edits;

        private final Function<FileRegion, FileRegion> affectedRegionMapper;

        public TextBasedChangesProcessor(final RobotProjectConfigWithLines config,
                final Function<FileRegion, FileRegion> affectedRegionMapper) {
            this.config = config;
            this.edits = new ArrayList<>();
            this.affectedRegionMapper = affectedRegionMapper;
        }

        boolean hasEditsCollected() {
            return !edits.isEmpty();
        }

        TextEdit[] getEdits() {
            return edits.toArray(new TextEdit[0]);
        }

        @Override
        public void pathModified(final Object affectedConfigModelPart, final Object newConfigModelPart) {
            final FileRegion fileRegion = config.getRegionFor(affectedConfigModelPart);

            if (fileRegion != null) {
                final FileRegion affectedRegion = affectedRegionMapper.apply(fileRegion);

                final int startOffset = affectedRegion.getStart().getOffset();
                final int endOffset = affectedRegion.getEnd().getOffset();
                final String text = new RobotProjectConfigWriter().writeFragment(newConfigModelPart);
                edits.add(new ReplaceEdit(startOffset, endOffset - startOffset + 1, text));
            }
        }

        @Override
        public void pathRemoved(final RobotProjectConfig configuration,
                final Object affectedConfigModelPart) {
            final FileRegion fileRegion = config.getRegionFor(affectedConfigModelPart);

            if (fileRegion != null) {
                final FileRegion affectedRegion = affectedRegionMapper.apply(fileRegion);

                final int startOffset = affectedRegion.getStart().getOffset();
                final int endOffset = affectedRegion.getEnd().getOffset();
                edits.add(new DeleteEdit(startOffset, endOffset - startOffset + 1));
            }
        }
    }
}