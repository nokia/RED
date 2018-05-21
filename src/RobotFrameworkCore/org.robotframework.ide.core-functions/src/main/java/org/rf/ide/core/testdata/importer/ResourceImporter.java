/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.importer;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.AImported.Type;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;

public class ResourceImporter {

    private final RobotParser parser;

    private final AbsoluteUriFinder uriFinder;

    public ResourceImporter(final RobotParser parser) {
        this.parser = parser;
        this.uriFinder = new AbsoluteUriFinder();
    }

    public List<ResourceImportReference> importResources(final PathsProvider pathsProvider,
            final RobotProjectHolder robotProject, final RobotFileOutput robotFile) {

        final SettingTable settingTable = robotFile.getFileModel().getSettingTable();
        if (!settingTable.isPresent()) {
            return new ArrayList<>();
        }

        final File importingFile = robotFile.getProcessedFile().getAbsoluteFile();
        final Map<String, String> variableMappings = robotProject.getVariableMappings();

        final List<ResourceImportReference> importedReferences = new ArrayList<>();
        for (final AImported imported : settingTable.getImports()) {

            try {
                final Optional<ResourceImportReference> importRef = createImportReference(imported, importingFile,
                        pathsProvider, variableMappings);
                if (importRef.isPresent()) {
                    importedReferences.add(importRef.get());
                }
            } catch (final UnableToImportException e) {
                robotFile.addBuildMessage(e.buildMessage);
            }
        }
        robotFile.addResourceReferences(importedReferences);
        return importedReferences;
    }

    private Optional<ResourceImportReference> createImportReference(final AImported imported, final File importingFile,
            final PathsProvider pathsProvider, final Map<String, String> variableMappings) {

        if (imported.getType() == Type.RESOURCE) {
            final String path = imported.getPathOrName().getText();

            URI importUri = null;
            try {
                final Optional<URI> foundUri = uriFinder.find(pathsProvider, variableMappings, importingFile, path);
                if (foundUri.isPresent()) {
                    importUri = foundUri.get();
                } else {
                    throw new UnableToImportException(BuildMessage
                            .createErrorMessage("Couldn't import resource file " + path, "" + importingFile));
                }

            } catch (final Exception e) {
                throw new UnableToImportException(BuildMessage.createErrorMessage(
                        "Problem with importing resource file " + importingFile + " with error stack: " + e,
                        "" + importingFile));
            }

            final File toImport = new File(importUri);
            final List<RobotFileOutput> parsed = parser.parse(toImport);
            if (parsed.isEmpty()) {
                throw new UnableToImportException(
                        BuildMessage.createErrorMessage("Couldn't import resource file.", toImport.getAbsolutePath()));
            } else {
                return Optional.of(new ResourceImportReference((ResourceImport) imported, parsed.get(0)));
            }
        }
        return Optional.empty();
    }

    public void importDebugResource(final RobotFileOutput robotFile, final File toImport) {
        final List<RobotFileOutput> parsedFiles = parser.parse(toImport);
        if (parsedFiles.isEmpty()) {
            robotFile.addBuildMessage(
                    BuildMessage.createErrorMessage("Couldn't import resource file.", toImport.getAbsolutePath()));
        } else {
            final ResourceImportReference resourceReference = new ResourceImportReference(null, parsedFiles.get(0));
            final int position = robotFile.findResourceReferencePositionToReplace(resourceReference);
            if (position < 0) {
                robotFile.addResourceReference(resourceReference);
            }
        }
    }

    private class UnableToImportException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final BuildMessage buildMessage;

        public UnableToImportException(final BuildMessage buildMessage) {
            this.buildMessage = buildMessage;
        }
    }
}
