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

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.LibdocFormat;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.rf.ide.core.rflint.RfLintRule;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
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

            final List<String> output = runExternalProcess(cmdLine);

            final String json = Iterables.getOnlyElement(output);
            return new LinkedHashMap<>(new ObjectMapper().readValue(json, STRING_TO_OBJECT_MAPPING_TYPE));
        } catch (final IOException e) {
            return new LinkedHashMap<>();
        }

    }

    @Override
    public Map<String, Object> getGlobalVariables() {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_variables.py");
            final List<String> cmdLine = createCommandLine(scriptFile, "-global");

            final List<String> output = runExternalProcess(cmdLine);

            final String json = Iterables.getOnlyElement(output);
            return new LinkedHashMap<>(new ObjectMapper().readValue(json, STRING_TO_OBJECT_MAPPING_TYPE));
        } catch (final IOException e) {
            return new LinkedHashMap<>();
        }
    }

    @Override
    public List<String> getStandardLibrariesNames() {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_libraries.py");
            final List<String> cmdLine = createCommandLine(scriptFile, "-names");

            final List<String> output = runExternalProcess(cmdLine);

            final String json = Iterables.getOnlyElement(output);
            return new ObjectMapper().readValue(json, STRING_LIST_TYPE);
        } catch (final IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public String getStandardLibraryPath(final String libraryName) {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_libraries.py");
            final List<String> cmdLine = createCommandLine(scriptFile, "-path", libraryName);

            final List<String> output = runExternalProcess(cmdLine);

            return Iterables.getOnlyElement(output);
        } catch (final IOException e) {
            return null;
        }
    }

    @Override
    public String getRobotVersion() {
        try {
            final List<String> cmdLine = createCommandLine(null, "-m", "robot.run", "--version");

            // help or version information printing returns custom robot exit code
            final List<String> output = runExternalProcess(cmdLine, 251);

            final String version = Iterables.getOnlyElement(output);
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

            final byte[] decodedFileContent = runLibdoc(cmdLine);
            writeLibdocToFile(resultFilePath, decodedFileContent);
        } catch (final IOException e) {
            // simply libdoc will not be generated
        }
    }

    private byte[] runLibdoc(final List<String> cmdLine) throws IOException {
        final List<String> output = runExternalProcess(cmdLine);

        // when properly finished there is encoded file content in last line
        final String base64EncodedLibFileContent = Iterables.getLast(output);
        return Base64.getDecoder().decode(base64EncodedLibFileContent);
    }

    private void writeLibdocToFile(final String resultFilePath, final byte[] decodedFileContent) throws IOException {
        final File libdocFile = new File(resultFilePath);
        if (!libdocFile.exists()) {
            libdocFile.createNewFile();
        }
        Files.write(decodedFileContent, libdocFile);
    }

    @Override
    public String createHtmlDoc(final String doc, final DocFormat format) {
        File docFile = null;
        try {
            docFile = RobotRuntimeEnvironment.createTemporaryFile();
        } catch (final IOException e) {
            return "";
        }

        try {
            Files.asCharSink(docFile, Charsets.UTF_8).write(doc);

            final File scriptFile = RobotRuntimeEnvironment.createTemporaryFile("red_libraries.py");
            final List<String> cmdLine = createCommandLine(scriptFile, new EnvironmentSearchPaths(), "-htmldoc",
                    format.name(), docFile.getAbsolutePath());

            final List<String> docLines = new ArrayList<>();
            RobotRuntimeEnvironment.runExternalProcess(cmdLine, line -> docLines.add(line));
            return String.join("\n", docLines);
        } catch (final IOException e) {
            return "";
        } finally {
            docFile.delete();
        }
    }

    @Override
    public List<File> getModulesSearchPaths() {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_modules.py");
            final List<String> cmdLine = createCommandLine(scriptFile, "-pythonpath");

            final List<String> output = runExternalProcess(cmdLine);

            final String json = Iterables.getOnlyElement(output);
            final List<String> pathsFromJson = new ObjectMapper().readValue(json, STRING_LIST_TYPE);
            return pathsFromJson.stream()
                    .filter(input -> !"".equals(input) && !".".equals(input))
                    .map(path -> new File(path))
                    .collect(toList());
        } catch (final IOException e) {
            throw new RobotEnvironmentException("Unable to obtain modules search paths", e);
        }
    }

    @Override
    public File getModulePath(final String moduleName, final EnvironmentSearchPaths additionalPaths) {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_modules.py");

            final List<String> cmdLine = createCommandLine(scriptFile, additionalPaths, "-modulename", moduleName);
            if (additionalPaths.hasPythonPaths()) {
                cmdLine.add(String.join(";", additionalPaths.getExtendedPythonPaths(interpreterType)));
            }

            final List<String> output = runExternalProcess(cmdLine);

            return new File(Iterables.getOnlyElement(output));
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

            final List<String> output = runExternalProcess(cmdLine);

            final String json = Iterables.getOnlyElement(output);
            return new ObjectMapper().readValue(json, STRING_LIST_TYPE);
        } catch (final IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void startLibraryAutoDiscovering(final int port, final File dataSource, final File projectLocation,
            final boolean recursiveInVirtualenv, final List<String> excludedPaths,
            final EnvironmentSearchPaths additionalPaths) {
        try {
            RobotRuntimeEnvironment.copyScriptFile("TestRunnerAgent.py");
            RobotRuntimeEnvironment.copyScriptFile("SuiteVisitorImportProxy.py");
            final File scriptFile = RobotRuntimeEnvironment.copyScriptFile("red_library_autodiscover.py");

            final List<String> cmdLine = createCommandLine(scriptFile, String.valueOf(port),
                    dataSource.getAbsolutePath(), projectLocation.getAbsolutePath(),
                    String.valueOf(recursiveInVirtualenv));
            if (!excludedPaths.isEmpty()) {
                cmdLine.add("-excluded");
                cmdLine.add(String.join(";", excludedPaths));
            }
            if (additionalPaths.hasPythonPaths()) {
                cmdLine.add(String.join(";", additionalPaths.getExtendedPythonPaths(interpreterType)));
            }

            runExternalProcess(cmdLine);
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

            runExternalProcess(cmdLine);
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
            final List<String> rulesFiles, final List<String> additionalArguments) {
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
            cmdLine.addAll(RobotCommandRpcExecutor.createRfLintArguments(rules, rulesFiles, additionalArguments));
            cmdLine.add("-r");
            cmdLine.add(filepath.getAbsolutePath());

            runExternalProcess(cmdLine);
        } catch (final IOException | NumberFormatException e) {
            throw new RobotEnvironmentException("Unable to start RfLint");
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

    private List<String> runExternalProcess(final List<String> cmdLine) throws IOException {
        return runExternalProcess(cmdLine, 0);
    }

    private List<String> runExternalProcess(final List<String> cmdLine, final int successExitCode) throws IOException {
        final List<String> output = new ArrayList<>();
        final int exitCode = RobotRuntimeEnvironment.runExternalProcess(cmdLine, output::add);
        if (exitCode != successExitCode) {
            final String indent = Strings.repeat(" ", 12);
            final String indentedException = indent + String.join("\n" + indent, output);
            throw new RobotEnvironmentException("RED python session problem. Following exception has been thrown by "
                    + "python service:\n" + indentedException);
        }
        return output;
    }
}
