/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.importer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.AImported.Type;
import org.robotframework.ide.core.testData.model.table.setting.VariablesImport;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


public class VariablesImporter {

    public List<VariablesFileImportReference> importVariables(
            final RobotRuntimeEnvironment robotRunEnv,
            final RobotFileOutput robotFile) {
        List<VariablesFileImportReference> varsImported = new LinkedList<>();
        SettingTable settingTable = robotFile.getFileModel().getSettingTable();
        if (settingTable.isPresent()) {
            List<AImported> imports = settingTable.getImports();
            for (AImported imported : imports) {
                Type type = imported.getType();
                if (type == Type.VARIABLES) {
                    VariablesImport varImport = (VariablesImport) imported;
                    String path = varImport.getPathOrName().getRaw().toString();
                    List<String> varFileArguments = convertTokensToArguments(varImport);

                    File directory = robotFile.getProcessedFile()
                            .getAbsoluteFile().getParentFile();
                    if (directory.exists()) {
                        Path joinPath = Paths.get(directory.getAbsolutePath())
                                .resolveSibling(path);
                        path = joinPath.toAbsolutePath().toFile()
                                .getAbsolutePath();
                    }
                    Map<?, ?> variablesFromFile = robotRunEnv
                            .getVariablesFromFile(path, varFileArguments);
                    VariablesFileImportReference varImportRef = new VariablesFileImportReference(
                            varImport);
                    varImportRef.setVariablesFile(new File(path)
                            .getAbsoluteFile());
                    varImportRef.map(variablesFromFile);
                }
            }
        }

        return varsImported;
    }


    @VisibleForTesting
    protected List<String> convertTokensToArguments(
            final VariablesImport varImport) {
        List<String> arguments = new LinkedList<>();
        for (RobotToken rtArgument : varImport.getArguments()) {
            arguments.add(rtArgument.getRaw().toString());
        }

        return arguments;
    }
}
