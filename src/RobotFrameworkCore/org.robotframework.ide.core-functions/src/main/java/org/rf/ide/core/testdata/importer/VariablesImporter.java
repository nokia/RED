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
import java.util.Optional;
import java.util.regex.Pattern;

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

public class VariablesImporter {

    private static final Pattern ILLEGAL_PATH_TEXT = Pattern.compile("\\s+([\\\\]|/)");

    private final AbsoluteUriFinder uriFinder = new AbsoluteUriFinder();

    public List<VariablesFileImportReference> importVariables(final PathsProvider pathsProvider,
            final RobotProjectHolder robotProject, final RobotFileOutput robotFile) {

        final List<VariablesFileImportReference> varsImported = new ArrayList<>();
        final SettingTable settingTable = robotFile.getFileModel().getSettingTable();
        if (settingTable.isPresent()) {
            final List<AImported> imports = settingTable.getImports();
            for (final AImported imported : imports) {
                final Type type = imported.getType();
                if (type == Type.VARIABLES) {

                    // skip iteration if declared path is incorrect
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

                    // skip iteration if file does not exist or could not be obtained
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
                        reportError(String.format("Problem importing variable file '%s'. %s", path, e.getMessage()),
                                currentRobotFile, varImport, robotFile);
                        continue;
                    }

                    // Get cached variables file import reference
                    final File varFile = new File(importUri);
                    VariablesFileImportReference varImportRef;
                    try {
                        varImportRef = findInProjectVariablesImport(pathsProvider, robotProject, varImport,
                                varFile.toPath().normalize().toFile());
                    } catch (final InvalidPathException e) {
                        reportError(String.format("Problem importing variable file '%s'. %s", path, e.getMessage()),
                                currentRobotFile, varImport, robotFile);
                        continue;
                    }

                    if (varImportRef == null) {
                        // could not find import reference in project, so will ask interpreter
                        Map<?, ?> variablesFromFile = new HashMap<>();
                        try {
                            variablesFromFile = robotProject.getRobotRuntime()
                                    .getVariablesFromFile(varFile.getAbsolutePath(), varFileArguments);
                        } catch (final Exception e) {
                            reportError(String.format("Problem importing variable file '%s'. %s", path, e.getMessage()),
                                    currentRobotFile, varImport, robotFile);
                            continue;
                        }
                        varImportRef = new VariablesFileImportReference(varImport);
                        varImportRef.setVariablesFile(varFile.getAbsoluteFile());
                        varImportRef.map(variablesFromFile);
                    } else {
                        varImportRef = varImportRef.copy(varImport);
                    }

                    // Report that this variable file import contains no obtainable information
                    if (varImportRef.getVariables().isEmpty()) {
                        reportWarning(String.format("Could not find any variable in variable file '%s'", path),
                                currentRobotFile, varImport, robotFile);
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

        VariablesFileImportReference varImportRef = null;
        final RobotFileOutput fileWhichImportsVariables = robotProject.findFileWithImportedVariableFile(pathsProvider,
                varFile);
        if (fileWhichImportsVariables != null) {
            final VariablesFileImportReference variableFile = findVariableFileByPath(pathsProvider, robotProject,
                    fileWhichImportsVariables, varFile);
            if (isThisVariableFileTheSearchedOne(variableFile, varImport, varFile)) {
                varImportRef = variableFile;
            }
        }
        return varImportRef;
    }

    @VisibleForTesting
    protected VariablesFileImportReference findVariableFileByPath(final PathsProvider pathsProvider,
            final RobotProjectHolder robotProject, final RobotFileOutput robotFile, final File varFile) {

        final List<VariablesFileImportReference> variablesImportReferences = robotFile
                .getVariablesImportReferences(robotProject, pathsProvider);
        for (final VariablesFileImportReference varFileImport : variablesImportReferences) {
            if (varFileImport.getVariablesFile().getAbsolutePath().equals(varFile.getAbsolutePath())) {
                return varFileImport;
            }
        }
        return null;
    }

    private static boolean isThisVariableFileTheSearchedOne(final VariablesFileImportReference current,
            final VariablesImport varImport, final File varFile) {
        return (current != null) && checkIfImportDeclarationAreTheSame(varImport, current.getImportDeclaration())
                && (varFile.lastModified() == current.getLastModificationEpochTime());
    }

    @VisibleForTesting
    protected static boolean checkIfImportDeclarationAreTheSame(final VariablesImport varImportCurrent,
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
                    final String argumentTextCurrent = argCurrent.getText();
                    final RobotToken argPrevious = argsPrevious.get(i);
                    final String argumentTextPrevious = argPrevious.getText();
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

    private static void reportError(final String message, final File currentRobotFile, final VariablesImport varImport,
            final RobotFileOutput robotFile) {
        final BuildMessage buildMsg = BuildMessage.createErrorMessage(message, currentRobotFile.getPath());
        buildMsg.setFileRegion(new FileRegion(varImport.getPathOrName().getFilePosition(), varImport.getEndPosition()));
        addBuildMessageIfNotExists(robotFile, buildMsg);
    }

    private static void reportWarning(final String message, final File currentRobotFile,
            final VariablesImport varImport, final RobotFileOutput robotFile) {
        final BuildMessage buildMsg = BuildMessage.createWarnMessage(message, currentRobotFile.getPath());
        buildMsg.setFileRegion(new FileRegion(varImport.getPathOrName().getFilePosition(), varImport.getEndPosition()));
        addBuildMessageIfNotExists(robotFile, buildMsg);
    }

    private static void addBuildMessageIfNotExists(final RobotFileOutput robotFile, final BuildMessage buildMsg) {
        final List<BuildMessage> buildingMessages = robotFile.getBuildingMessages();
        if (!buildingMessages.contains(buildMsg)) {
            robotFile.addBuildMessage(buildMsg);
        }
    }
}
