/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.importer;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.AImported.Type;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;

public class ResourceImporter {

    public List<ResourceImportReference> importResources(final RobotParser parser, final RobotFileOutput robotFile,
            final List<File> alreadyImported) {
        final List<ResourceImportReference> importedReferences = new ArrayList<>();

        final SettingTable settingTable = robotFile.getFileModel().getSettingTable();
        if (settingTable.isPresent()) {
            final List<AImported> imports = settingTable.getImports();
            for (final AImported imported : imports) {
                final Type type = imported.getType();
                if (type == Type.RESOURCE) {
                    String path = imported.getPathOrName().getRaw().toString();

                    final File currentFile = robotFile.getProcessedFile().getAbsoluteFile();
                    if (currentFile.exists()) {
                        try {
                            final Path joinPath = Paths.get(currentFile.getAbsolutePath()).resolveSibling(path);
                            path = joinPath.normalize().toAbsolutePath().toFile().getAbsolutePath();
                        } catch (final InvalidPathException ipe) {
                            robotFile.addBuildMessage(BuildMessage.createErrorMessage(
                                    "Problem with importing resource file " + currentFile + " with error stack: " + ipe,
                                    "" + robotFile.getProcessedFile()));
                            continue;
                        }
                    }

                    final File toImport = new File(path);
                    final List<RobotFileOutput> parsed = parser.parse(toImport, alreadyImported);
                    if (parsed.isEmpty()) {
                        robotFile.addBuildMessage(BuildMessage.createErrorMessage("Couldn't import resource file.",
                                toImport.getAbsolutePath()));
                    } else {
                        importedReferences.add(new ResourceImportReference((ResourceImport) imported, parsed.get(0)));
                    }
                }
            }
        }
        robotFile.addResourceReferences(importedReferences);

        return importedReferences;
    }

    public void importDebugResource(final RobotParser parser, final RobotFileOutput robotFile, final String path) {
        final File toImport = new File(path);
        final List<RobotFileOutput> parsedFiles = parser.parse(toImport);
        if (parsedFiles.isEmpty()) {
            robotFile.addBuildMessage(
                    BuildMessage.createErrorMessage("Couldn't import resource file.", toImport.getAbsolutePath()));
        } else {
            ResourceImportReference resourceReference = new ResourceImportReference(null, parsedFiles.get(0));
            int position = robotFile.findResourceReferencePositionToReplace(resourceReference);
            if (position < 0) {
                robotFile.addResourceReference(resourceReference);
            }
        }
    }
}
