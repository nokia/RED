/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.importer;

import java.io.File;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotExpressions;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.AImported.Type;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public class VariablesImporter {

    private static final Pattern ILLEGAL_PATH_TEXT = Pattern.compile("\\s+([\\\\]|/)");

    private final AbsoluteUriFinder uriFinder = new AbsoluteUriFinder();

    public List<VariablesFileImportReference> importVariables(final PathsProvider pathsProvider,
            final RobotProjectHolder robotProject, final RobotFileOutput robotFile) {
        return importVariables(pathsProvider, robotProject.getRobotRuntime(), robotProject, robotFile);
    }

    public List<VariablesFileImportReference> importVariables(final PathsProvider pathsProvider,
            final RobotRuntimeEnvironment robotRunEnv, final RobotProjectHolder robotProject,
            final RobotFileOutput robotFile) {

        final List<VariablesFileImportReference> varsImported = new ArrayList<>();
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
                    final String path = varImport.getPathOrName().getRaw().toString();
                    if (!isCorrectPath(path)) {
                        continue;
                    }

                    final Map<String, String> variableMappings = robotProject.getVariableMappings();
                    final List<String> varFileArguments = convertTokensToArguments(varImport, variableMappings);

                    URI importUri = null;
                    final File currentRobotFile = robotFile.getProcessedFile().getAbsoluteFile();
                    try {
                        final Optional<URI> foundUri = uriFinder.find(pathsProvider, variableMappings, currentRobotFile,
                                path);
                        if (foundUri.isPresent()) {
                            importUri = foundUri.get();
                        } else {
                            continue;
                        }

                    } catch (final Exception e) {
                        final BuildMessage errorMsg = BuildMessage.createErrorMessage(
                                "Problem with importing variable file " + path + " with error stack: " + e,
                                "" + currentRobotFile);
                        errorMsg.setFileRegion(
                                new FileRegion(varImport.getBeginPosition(), varImport.getEndPosition()));
                        robotFile.addBuildMessage(errorMsg);

                        continue;
                    }

                    final File varFile = new File(importUri);
                    VariablesFileImportReference varImportRef;
                    try {
                        varImportRef = findInProjectVariablesImport(pathsProvider, robotProject, varImport,
                                varFile.toPath().normalize().toFile());
                    } catch (final InvalidPathException ipe) {
                        final BuildMessage errorMsg = BuildMessage.createErrorMessage(
                                "Problem with importing variable file " + path + " with error stack: " + ipe,
                                "" + currentRobotFile);
                        errorMsg.setFileRegion(
                                new FileRegion(varImport.getBeginPosition(), varImport.getEndPosition()));
                        robotFile.addBuildMessage(errorMsg);
                        continue;
                    }

                    if (varImportRef == null) {
                        Map<?, ?> variablesFromFile = new HashMap<>();
                        try {
                            variablesFromFile = robotRunEnv.getVariablesFromFile(varFile.getAbsolutePath(),
                                    varFileArguments);
                        } catch (final Exception e) {
                            final BuildMessage errorMsg = BuildMessage.createErrorMessage(
                                    "Problem with importing variable file " + path + " with error stack: " + e,
                                    "" + currentRobotFile);
                            errorMsg.setFileRegion(
                                    new FileRegion(varImport.getBeginPosition(), varImport.getEndPosition()));
                            robotFile.addBuildMessage(errorMsg);
                            continue;
                        }
                        varImportRef = new VariablesFileImportReference(varImport);
                        varImportRef.setVariablesFile(varFile.getAbsoluteFile());
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

    @VisibleForTesting
    protected boolean isCorrectPath(final String path) {
        if (path != null && !path.trim().isEmpty()) {
            final String convertedPath = RobotExpressions.unescapeSpaces(path);
            return !ILLEGAL_PATH_TEXT.matcher(convertedPath).find();
        }
        return false;
    }

    private VariablesFileImportReference findInProjectVariablesImport(final PathsProvider pathsProvider,
            final RobotProjectHolder robotProject, final VariablesImport varImport, final File varFile) {

        final List<RobotFileOutput> filesWhichImportingVariables = robotProject
                .findFilesWithImportedVariableFile(pathsProvider, varFile);
        VariablesFileImportReference varImportRef = null;
        for (final RobotFileOutput rfo : filesWhichImportingVariables) {
            final VariablesFileImportReference variableFile = findVariableFileByPath(pathsProvider, robotProject, rfo,
                    varFile);
            if (variableFile != null) {
                if (checkIfImportDeclarationAreTheSame(varImport, variableFile.getImportDeclaration())) {
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
    protected VariablesFileImportReference findVariableFileByPath(final PathsProvider pathsProvider,
            final RobotProjectHolder robotProject, final RobotFileOutput rfo, final File varFile) {

        VariablesFileImportReference varFileImportReference = null;
        final List<VariablesFileImportReference> variablesImportReferences = rfo
                .getVariablesImportReferences(robotProject, pathsProvider);
        for (final VariablesFileImportReference varFileImport : variablesImportReferences) {
            if (varFileImport.getVariablesFile().getAbsolutePath().equals(varFile.getAbsolutePath())) {
                varFileImportReference = varFileImport;
                break;
            }
        }

        return varFileImportReference;
    }

    @VisibleForTesting
    protected boolean checkIfImportDeclarationAreTheSame(final VariablesImport varImportCurrent,
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
                    final String argumentTextCurrent = argCurrent.getText().toString();
                    final RobotToken argPrevious = argsPrevious.get(i);
                    final String argumentTextPrevious = argPrevious.getText().toString();
                    // TODO: add resolve in case parameter is variable
                    if (argumentTextCurrent != null && argumentTextPrevious != null) {
                        if (!argumentTextCurrent.equals(argumentTextPrevious)) {
                            result = false;
                        }
                    } else if (argumentTextCurrent == null && argumentTextPrevious == null) {
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
    protected List<String> convertTokensToArguments(final VariablesImport varImport,
            final Map<String, String> variableMappings) {
        final List<String> arguments = new ArrayList<>();
        for (final RobotToken rtArgument : varImport.getArguments()) {
            String arg = rtArgument.getRaw().toString();
            if (RobotExpressions.isParameterized(arg)) {
                arg = RobotExpressions.resolve(variableMappings, arg);
            }
            arguments.add(arg);
        }

        return arguments;
    }
}
