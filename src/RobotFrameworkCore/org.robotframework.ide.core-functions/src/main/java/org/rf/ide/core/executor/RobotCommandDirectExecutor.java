/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.LibdocFormat;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentDetailedException;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.rflint.RfLintRule;

import com.google.common.base.Strings;
import com.google.common.io.Files;

/**
 * @author Michal Anglart
 */
class RobotCommandDirectExecutor implements RobotCommandExecutor {

    private static final TypeReference<Map<String, Object>> STRING_TO_OBJECT_MAPPING_TYPE = new TypeReference<Map<String, Object>>() {
    };

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<List<String>>() {
    };

    private final String interpreterPath;

    private final SuiteExecutor interpreterType;

    RobotCommandDirectExecutor(final String interpreterPath, final SuiteExecutor interpreterType) {
        this.interpreterPath = interpreterPath;
        this.interpreterType = interpreterType;
    }

    @Override
    public Map<String, Object> getVariables(final String filePath, final List<String> fileArguments) {
        try {
            final String normalizedPath = filePath.replace('\\', '/');

            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_variables.py");
            final List<String> cmdLine = createCommandLine(scriptFile, "-variables", normalizedPath);
            cmdLine.addAll(fileArguments);

            final StringBuilder jsonEncodedOutput = new StringBuilder();
            RobotRuntimeEnvironment.runExternalProcess(cmdLine, line -> jsonEncodedOutput.append(line));

            final String resultVars = jsonEncodedOutput.toString().trim();
            final Map<String, Object> variables = new ObjectMapper().readValue(resultVars,
                    STRING_TO_OBJECT_MAPPING_TYPE);
            return new LinkedHashMap<>(variables);
        } catch (final IOException e) {
            return new LinkedHashMap<>();
        }

    }

    @Override
    public Map<String, Object> getGlobalVariables() {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_variables.py");
            final List<String> cmdLine = createCommandLine(scriptFile, "-global");

            final StringBuilder jsonEncodedOutput = new StringBuilder();
            RobotRuntimeEnvironment.runExternalProcess(cmdLine, line -> jsonEncodedOutput.append(line));

            final String resultVars = jsonEncodedOutput.toString().trim();
            final Map<String, Object> variables = new ObjectMapper().readValue(resultVars,
                    STRING_TO_OBJECT_MAPPING_TYPE);
            return new LinkedHashMap<>(variables);
        } catch (final IOException e) {
            return new LinkedHashMap<>();
        }
    }

    @Override
    public List<String> getStandardLibrariesNames() {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_libraries.py");
            final List<String> cmdLine = createCommandLine(scriptFile, "-names");

            final List<String> stdLibs = new ArrayList<>();
            RobotRuntimeEnvironment.runExternalProcess(cmdLine, line -> stdLibs.add(line.trim()));

            return stdLibs;
        } catch (final IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public String getStandardLibraryPath(final String libraryName) {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_libraries.py");
            final List<String> cmdLine = createCommandLine(scriptFile, "-path", libraryName);

            final StringBuilder path = new StringBuilder();
            RobotRuntimeEnvironment.runExternalProcess(cmdLine, line -> path.append(line));

            return path.toString().trim();
        } catch (final IOException e) {
            return null;
        }
    }

    @Override
    public String getRobotVersion() {
        try {
            final List<String> cmdLine = createCommandLine(null, "-m", "robot.run", "--version");

            final StringBuilder versionOutput = new StringBuilder();
            RobotRuntimeEnvironment.runExternalProcess(cmdLine, line -> versionOutput.append(line));

            final String version = versionOutput.toString();
            return version.startsWith("Robot Framework") ? version.trim() : null;
        } catch (final IOException e) {
            return null;
        }
    }

    @Override
    public void createLibdoc(final String resultFilePath, final LibdocFormat format, final String libName,
            final String libPath, final EnvironmentSearchPaths additionalPaths) {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_libraries.py");
            final List<String> cmdLine = createCommandLine(scriptFile, additionalPaths, "-libdoc", libName,
                    format.name().toLowerCase());
            if (!libPath.isEmpty()) {
                cmdLine.add(libPath);
            }

            cmdLine.addAll(additionalPaths.getExtendedPythonPaths(interpreterType));
            cmdLine.addAll(additionalPaths.getClassPaths());

            final byte[] decodedFileContent = runLibdoc(libName, cmdLine);
            writeLibdocToFile(resultFilePath, decodedFileContent);
        } catch (final IOException e) {
            // simply libdoc will not be generated
        }
    }

    private byte[] runLibdoc(final String libName, final List<String> cmdLine) {
        try {
            final List<String> lines = new ArrayList<>();
            RobotRuntimeEnvironment.runExternalProcess(cmdLine, line -> lines.add(line));

            // when properly finished there is a path to the file in first line and encoded content
            // in second
            if (lines.size() != 2) {
                throw new RobotEnvironmentDetailedException(String.join("\n", lines),
                        "Unable to generate library specification file for library '" + libName + "'");
            } else {
                final String base64EncodedLibFileContent = lines.get(1);
                return Base64.getDecoder().decode(base64EncodedLibFileContent);
            }
        } catch (final IOException e) {
            throw new RobotEnvironmentDetailedException(e.getMessage(),
                    "Unable to generate library specification file for library '" + libName + "'", e);
        }
    }

    private void writeLibdocToFile(final String resultFilePath, final byte[] decodedFileContent) throws IOException {
        final File libdocFile = new File(resultFilePath);
        if (!libdocFile.exists()) {
            libdocFile.createNewFile();
        }
        Files.write(decodedFileContent, libdocFile);
    }

    @Override
    public List<File> getModulesSearchPaths() {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_modules.py");
            final List<String> cmdLine = createCommandLine(scriptFile, "-pythonpath");

            final StringBuilder jsonEncodedOutput = new StringBuilder();
            final int exitCode = RobotRuntimeEnvironment.runExternalProcess(cmdLine,
                    line -> jsonEncodedOutput.append(line));

            if (exitCode != 0) {
                throw new RobotEnvironmentException("Unable to obtain modules search paths");
            }
            final List<String> pathsFromJson = new ObjectMapper().readValue(jsonEncodedOutput.toString(),
                    STRING_LIST_TYPE);
            return pathsFromJson.stream()
                    .filter(input -> !"".equals(input) && !".".equals(input))
                    .map(path -> new File(path))
                    .collect(toList());
        } catch (final IOException e) {
            throw new RobotEnvironmentException("Unable to obtain modules search paths", e);
        }
    }

    @Override
    public Optional<File> getModulePath(final String moduleName, final EnvironmentSearchPaths additionalPaths) {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_modules.py");

            final List<String> cmdLine = createCommandLine(scriptFile, additionalPaths, "-modulename", moduleName);
            if (additionalPaths.hasPythonPaths()) {
                cmdLine.add(String.join(";", additionalPaths.getExtendedPythonPaths(interpreterType)));
            }

            final List<String> lines = new ArrayList<>();
            RobotRuntimeEnvironment.runExternalProcess(cmdLine, line -> lines.add(line));
            if (lines.size() == 1) {
                // there should be a single line with path only
                return Optional.of(new File(lines.get(0).toString()));
            } else {
                final String indent = Strings.repeat(" ", 12);
                final String exception = indent + String.join("\n" + indent, lines);
                throw new RobotEnvironmentException(
                        "RED python session problem. Following exception has been thrown by python service:\n"
                                + exception);
            }
        } catch (final IOException e) {
            throw new RobotEnvironmentException("Unable to find path of '" + moduleName + "' module", e);
        }
    }

    @Override
    public List<String> getClassesFromModule(final File moduleLocation, final EnvironmentSearchPaths additionalPaths) {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_module_classes.py");

            final List<String> cmdLine = createCommandLine(scriptFile, additionalPaths,
                    moduleLocation.getAbsolutePath());
            if (additionalPaths.hasPythonPaths()) {
                cmdLine.add(String.join(";", additionalPaths.getExtendedPythonPaths(interpreterType)));
            }

            final StringBuilder jsonEncodedOutput = new StringBuilder();
            final int exitCode = RobotRuntimeEnvironment.runExternalProcess(cmdLine,
                    line -> jsonEncodedOutput.append(line));

            if (exitCode != 0) {
                throw new RobotEnvironmentException(
                        "Python interpreter returned following errors:\n\n" + String.join("\n", jsonEncodedOutput));
            }
            return new ObjectMapper().readValue(jsonEncodedOutput.toString(), STRING_LIST_TYPE);
        } catch (final IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void startLibraryAutoDiscovering(final int port, final File dataSource, final File projectLocation,
            final boolean recursiveInVirtualenv, final List<String> excludedPaths) {
        try {
            RobotRuntimeEnvironment.copyScriptFile("TestRunnerAgent.py");
            RobotRuntimeEnvironment.copyScriptFile("SuiteVisitorImportProxy.py");
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_library_autodiscover.py");

            final List<String> cmdLine = createCommandLine(scriptFile, String.valueOf(port),
                    dataSource.getAbsolutePath(), projectLocation.getAbsolutePath(),
                    String.valueOf(recursiveInVirtualenv));
            cmdLine.add(String.join(";", excludedPaths));

            RobotRuntimeEnvironment.runExternalProcess(cmdLine, line -> {});
        } catch (final IOException | NumberFormatException e) {
            throw new RobotEnvironmentException("Unable to start library autodiscovering.");
        }
    }

    @Override
    public void startKeywordAutoDiscovering(final int port, final File dataSource,
            final EnvironmentSearchPaths additionalPaths) {
        try {
            RobotRuntimeEnvironment.copyScriptFile("TestRunnerAgent.py");
            RobotRuntimeEnvironment.copyScriptFile("SuiteVisitorImportProxy.py");
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_keyword_autodiscover.py");

            final List<String> cmdLine = createCommandLine(scriptFile, additionalPaths, String.valueOf(port),
                    dataSource.getAbsolutePath());
            if (additionalPaths.hasPythonPaths()) {
                cmdLine.add(String.join(";", additionalPaths.getExtendedPythonPaths(interpreterType)));
            }

            RobotRuntimeEnvironment.runExternalProcess(cmdLine, line -> {});
        } catch (final IOException | NumberFormatException e) {
            throw new RobotEnvironmentException("Unable to start keyword autodiscovering.");
        }
    }

    @Override
    public void stopAutoDiscovering() {
        // nothing to do
    }

    @Override
    public void runRfLint(final String host, final int port, final File projectLocation,
            final List<String> excludedPaths, final File filepath, final List<RfLintRule> rules,
            final List<String> rulesFiles) {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("rflint_integration.py");

            final List<String> cmdLine = createCommandLine(scriptFile);

            cmdLine.add(host);
            cmdLine.add(Integer.toString(port));
            cmdLine.add(projectLocation.getAbsolutePath());
            if (!excludedPaths.isEmpty()) {
                cmdLine.add("-exclude");
                cmdLine.add(String.join(";", excludedPaths));
            }
            for (final String path : rulesFiles) {
                cmdLine.add("-R");
                cmdLine.add(path);
            }
            for (final RfLintRule rule : rules) {
                if (rule.hasChangedSeverity()) {
                    cmdLine.add("-" + rule.getSeverity().severitySwitch());
                    cmdLine.add(rule.getRuleName());
                }
                if (rule.hasConfigurationArguments()) {
                    cmdLine.add("-c");
                    cmdLine.add(rule.getRuleName() + ":" + rule.getConfiguration());
                }
            }
            cmdLine.add("-r");
            cmdLine.add(filepath.getAbsolutePath());

            RobotRuntimeEnvironment.runExternalProcess(cmdLine, line -> {});
        } catch (final IOException | NumberFormatException e) {
            throw new RobotEnvironmentException("Unable to start RfLint");
        }
    }

    static String severitySwitch(final String severity) {
        switch (severity.toLowerCase()) {
            case "error":
                return "-e";
            case "warning":
                return "-w";
            case "ignore":
                return "-i";
            default:
                throw new IllegalStateException();
        }
    }

    private List<String> createCommandLine(final File scriptFile, final String... lines) {
        return createCommandLine(scriptFile, new EnvironmentSearchPaths(), lines);
    }

    private List<String> createCommandLine(final File scriptFile, final EnvironmentSearchPaths additionalPaths,
            final String... lines) {
        final List<String> cmdLine = new ArrayList<>();
        cmdLine.add(interpreterPath);
        if (interpreterType == SuiteExecutor.Jython && additionalPaths.hasClassPaths()) {
            cmdLine.add("-J-cp");
            final String classpath = String.join(RedSystemProperties.getPathsSeparator(),
                    additionalPaths.getClassPaths());
            cmdLine.add(classpath);
        }
        if (scriptFile != null) {
            cmdLine.add(scriptFile.getAbsolutePath());
        }
        cmdLine.addAll(Arrays.asList(lines));
        return cmdLine;
    }
}
