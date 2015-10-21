/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
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
import org.robotframework.ide.core.testData.model.RobotProjectHolder;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.AImported.Type;
import org.robotframework.ide.core.testData.model.table.setting.VariablesImport;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


public class VariablesImporter {

    public List<VariablesFileImportReference> importVariables(
            final RobotRuntimeEnvironment robotRunEnv,
            final RobotProjectHolder robotProject,
            final RobotFileOutput robotFile) {
        final List<VariablesFileImportReference> varsImported = new LinkedList<>();
        final SettingTable settingTable = robotFile.getFileModel().getSettingTable();
        if (settingTable.isPresent()) {
            final List<AImported> imports = settingTable.getImports();
            for (final AImported imported : imports) {
                final Type type = imported.getType();
                if (type == Type.VARIABLES) {
                    final VariablesImport varImport = (VariablesImport) imported;
                    if (varImport.getPathOrName() == null) {
                        continue;
                    }
                    String path = varImport.getPathOrName().getRaw().toString();
                    final List<String> varFileArguments = convertTokensToArguments(varImport);

                    final File currentRobotFile = robotFile.getProcessedFile()
                            .getAbsoluteFile();
                    if (currentRobotFile.exists()) {
                        final Path joinPath = Paths.get(currentRobotFile.getAbsolutePath())
                                .resolveSibling(path);
                        path = joinPath.toAbsolutePath().toFile()
                                .getAbsolutePath();
                    }

                    final File varFile = new File(path);
                    VariablesFileImportReference varImportRef = findInProjectVariablesImport(
                            robotProject, varImport, varFile.toPath()
                                    .normalize().toFile());

                    if (varImportRef == null) {
                        final Map<?, ?> variablesFromFile = robotRunEnv
                                .getVariablesFromFile(path, varFileArguments);
                        varImportRef = new VariablesFileImportReference(
                                varImport);
                        varImportRef
                                .setVariablesFile(varFile.getAbsoluteFile());
                        varImportRef.map(variablesFromFile);
                    } else {
                        varImportRef = varImportRef.copy(varImport);
                    }

                    varsImported.add(varImportRef);
                }
            }
        }

        return varsImported;
    }


    private VariablesFileImportReference findInProjectVariablesImport(
            final RobotProjectHolder robotProject,
            final VariablesImport varImport, final File varFile) {

        final List<RobotFileOutput> filesWhichImportingVariables = robotProject
                .findFilesWithImportedVariableFile(varFile);
        VariablesFileImportReference varImportRef = null;
        for (final RobotFileOutput rfo : filesWhichImportingVariables) {
            final VariablesFileImportReference variableFile = findVariableFileByPath(
                    rfo, varFile);
            if (variableFile != null) {
                if (checkIfImportDeclarationAreTheSame(varImport,
                        variableFile.getImportDeclaration())) {
                    if (varFile.lastModified() == varFile.lastModified()) {
                        varImportRef = variableFile;
                        break;
                    }
                }
            }
        }
        return varImportRef;
    }


    @VisibleForTesting
    protected VariablesFileImportReference findVariableFileByPath(
            final RobotFileOutput rfo, final File varFile) {
        VariablesFileImportReference varFileImportReference = null;
        final List<VariablesFileImportReference> variablesImportReferences = rfo
                .getVariablesImportReferences();
        for (final VariablesFileImportReference varFileImport : variablesImportReferences) {
            if (varFileImport.getVariablesFile().getAbsolutePath()
                    .equals(varFile.getAbsolutePath())) {
                varFileImportReference = varFileImport;
                break;
            }
        }

        return varFileImportReference;
    }


    @VisibleForTesting
    protected boolean checkIfImportDeclarationAreTheSame(
            final VariablesImport varImportCurrent,
            final VariablesImport alreadyExecuted) {
        boolean result = false;
        if (varImportCurrent != null && alreadyExecuted != null) {
            final List<RobotToken> argsCurrent = varImportCurrent.getArguments();
            final List<RobotToken> argsPrevious = alreadyExecuted.getArguments();
            if (argsCurrent.size() == argsPrevious.size()) {
                result = true;
                final int size = argsCurrent.size();
                for (int i = 0; i < size; i++) {
                    final RobotToken argCurrent = argsCurrent.get(i);
                    final String argumentTextCurrent = argCurrent.getText()
                            .toString();
                    final RobotToken argPrevious = argsPrevious.get(i);
                    final String argumentTextPrevious = argPrevious.getText()
                            .toString();
                    // TODO: add resolve in case parameter is variable
                    if (argumentTextCurrent != null
                            && argumentTextPrevious != null) {
                        if (!argumentTextCurrent.equals(argumentTextPrevious)) {
                            result = false;
                        }
                    } else if (argumentTextCurrent == null
                            && argumentTextPrevious == null) {
                        result = true;
                    } else {
                        result = false;
                    }

                    if (!result) {
                        break;
                    }
                }
            }
        }

        return result;
    }


    @VisibleForTesting
    protected List<String> convertTokensToArguments(
            final VariablesImport varImport) {
        final List<String> arguments = new LinkedList<>();
        for (final RobotToken rtArgument : varImport.getArguments()) {
            arguments.add(rtArgument.getRaw().toString());
        }

        return arguments;
    }
}
