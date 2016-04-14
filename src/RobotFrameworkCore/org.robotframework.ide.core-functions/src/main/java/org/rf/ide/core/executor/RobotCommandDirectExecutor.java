/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * @author Michal Anglart
 *
 */
class RobotCommandDirectExecutor implements RobotCommandExecutor {

    private static final TypeReference<Map<String, Object>> STRING_TO_OBJECT_MAPPING_TYPE = 
            new TypeReference<Map<String, Object>>() { };

    private static final TypeReference<List<String>> STRING_LIST_TYPE =
            new TypeReference<List<String>>() { };

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

            final File scriptFile = RobotRuntimeEnvironment.copyResourceFile("red_variables.py");
            final List<String> cmdLine = newArrayList(interpreterPath, scriptFile.getAbsolutePath(), "-variables",
                    normalizedPath);
            cmdLine.addAll(fileArguments);

            final StringBuilder jsonEncodedOutput = new StringBuilder();
            final ILineHandler linesHandler = new ILineHandler() {

                @Override
                public void processLine(final String line) {
                    jsonEncodedOutput.append(line);
                }
            };
            RobotRuntimeEnvironment.runExternalProcess(cmdLine, linesHandler);
            final String resultVars = jsonEncodedOutput.toString().trim();
            final Map<String, Object> variables = new ObjectMapper().readValue(resultVars,
                    STRING_TO_OBJECT_MAPPING_TYPE);
            return Maps.newLinkedHashMap(variables);
        } catch (final IOException e) {
            return Maps.newLinkedHashMap();
        }

    }

    @Override
    public Map<String, Object> getGlobalVariables() {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyResourceFile("red_variables.py");
            if (scriptFile != null) {
                final List<String> cmdLine = newArrayList(interpreterPath, scriptFile.getAbsolutePath(), "-global");
                final StringBuilder jsonEncodedOutput = new StringBuilder();
                final ILineHandler linesHandler = new ILineHandler() {

                    @Override
                    public void processLine(final String line) {
                        jsonEncodedOutput.append(line);
                    }
                };
                RobotRuntimeEnvironment.runExternalProcess(cmdLine, linesHandler);
                final String resultVars = jsonEncodedOutput.toString().trim();

                final Map<String, Object> variables = new ObjectMapper().readValue(resultVars,
                        STRING_TO_OBJECT_MAPPING_TYPE);
                return Maps.newLinkedHashMap(variables);
            }
            return Maps.newLinkedHashMap();
        } catch (final IOException e) {
            return Maps.newLinkedHashMap();
        }
    }

    @Override
    public List<String> getStandardLibrariesNames() {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyResourceFile("red_libraries.py");
            final List<String> cmdLine = newArrayList(interpreterPath, scriptFile.getAbsolutePath(), "-names");
            final List<String> stdLibs = new ArrayList<>();
            final ILineHandler linesHandler = new ILineHandler() {
                @Override
                public void processLine(final String line) {
                    stdLibs.add(line.trim());
                }
            };
            RobotRuntimeEnvironment.runExternalProcess(cmdLine, linesHandler);
            return stdLibs;
        } catch (final IOException e) {
            return newArrayList();
        }
    }

    @Override
    public String getStandardLibraryPath(final String libraryName) {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyResourceFile("red_libraries.py");
            final List<String> cmdLine = newArrayList(interpreterPath, scriptFile.getAbsolutePath(), "-path",
                    libraryName);
            final StringBuilder path = new StringBuilder();
            final ILineHandler linesHandler = new ILineHandler() {

                @Override
                public void processLine(final String line) {
                    path.append(line);
                }
            };
            RobotRuntimeEnvironment.runExternalProcess(cmdLine, linesHandler);
            return path.toString().trim();
        } catch (final IOException e) {
            return null;
        }
    }

    @Override
    public String getRobotVersion() {
        final StringBuilder versionOutput = new StringBuilder();
        final ILineHandler linesHandler = new ILineHandler() {
            @Override
            public void processLine(final String line) {
                versionOutput.append(line);
            }
        };
        try {
            RobotRuntimeEnvironment.runExternalProcess(newArrayList(interpreterPath, "-m", "robot.run", "--version"),
                    linesHandler);
            final String version = versionOutput.toString();
            return version.startsWith("Robot Framework") ? version.trim() : null;
        } catch (final IOException e) {
            return null;
        }
    }

    @Override
    public void createLibdocForStdLibrary(final String resultFilePath, final String libName, final String libPath) {
        final List<String> cmdLine = newArrayList(interpreterPath, "-m", "robot.libdoc", "-f", "XML", libName,
                resultFilePath);
        runLibdoc(libName, cmdLine);
    }

    @Override
    public void createLibdocForPythonLibrary(final String resultFilePath, final String libName, final String libPath,
            final EnvironmentSearchPaths additionalPaths) {
        createLibdocForThirdPartyLib(resultFilePath, libName, libPath, additionalPaths);
    }

    @Override
    public void createLibdocForJavaLibrary(final String resultFilePath, final String libName, final String libPath,
            final EnvironmentSearchPaths additionalPaths) {
        createLibdocForThirdPartyLib(resultFilePath, libName, libPath, additionalPaths);
    }

    private void createLibdocForThirdPartyLib(final String resultFilePath, final String libName, final String libPath,
            final EnvironmentSearchPaths additionalPaths) {
        final List<String> additions = newArrayList(libPath);
        additions.addAll(additionalPaths.getPythonPaths());
        additions.addAll(additionalPaths.getClassPaths());
        if (interpreterType == SuiteExecutor.Jython) {
            additions.addAll(RedSystemProperties.getPythonPaths());
        }

        final String paths = RobotRuntimeEnvironment.wrapArgumentIfNeeded(Joiner.on(':').join(additions));
        final List<String> cmdLine = newArrayList(interpreterPath, "-m", "robot.libdoc", "-f", "XML", "-P", paths,
                libName, resultFilePath);
        runLibdoc(libName, cmdLine);
    }

    private void runLibdoc(final String libName, final List<String> cmdLine) {
        try {
            final List<String> lines = newArrayList();
            final ILineHandler handler = new ILineHandler() {

                @Override
                public void processLine(final String line) {
                    lines.add(line);
                }
            };
            final int returnCode = RobotRuntimeEnvironment.runExternalProcess(cmdLine, handler);
            if (returnCode != 0) {
                throw new RobotEnvironmentException("Unable to generate library specification file for library '"
                        + libName + "'" + "\nDetailed information:\n" + Joiner.on('\n').join(lines));
            }
        } catch (final IOException e) {
            throw new RobotEnvironmentException("Unable to generate library specification file for library '" + libName + "'",
                    e);
        }
    }

    @Override
    public List<File> getModulesSearchPaths() {
        try {
            final StringBuilder jsonEncodedOutput = new StringBuilder();
            final ILineHandler handler = new ILineHandler() {
                @Override
                public void processLine(final String line) {
                    jsonEncodedOutput.append(line);
                }
            };

            final File scriptFile = RobotRuntimeEnvironment.copyResourceFile("red_modules.py");
            final List<String> cmdLine = newArrayList(interpreterPath, scriptFile.getAbsolutePath(), "-pythonpath");
            final int returnCode = RobotRuntimeEnvironment.runExternalProcess(cmdLine, handler);
            if (returnCode != 0) {
                throw new RobotEnvironmentException("Unable to obtain modules search paths");
            }
            final List<String> pathsFromJson = new ObjectMapper().readValue(jsonEncodedOutput.toString(),
                    STRING_LIST_TYPE);
            final List<String> paths = newArrayList(Iterables.filter(pathsFromJson, new Predicate<String>() {
                @Override
                public boolean apply(final String input) {
                    return !("".equals(input) || ".".equals(input));
                }
            }));
            return newArrayList(Iterables.transform(paths, new Function<String, File>() {
                @Override
                public File apply(final String path) {
                    return new File(path);
                }
            }));
        } catch (final IOException e) {
            throw new RobotEnvironmentException("Unable to obtain modules search paths", e);
        }
    }

    @Override
    public Optional<File> getModulePath(final String moduleName, final EnvironmentSearchPaths additionalPaths) {
        final List<String> lines = new ArrayList<>();
        final ILineHandler linesHandler = new ILineHandler() {
            @Override
            public void processLine(final String line) {
                lines.add(line);
            }
        };
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyResourceFile("red_modules.py");
            
            final List<String> cmdLine = newArrayList(interpreterPath);
            if (interpreterType == SuiteExecutor.Jython) {
                cmdLine.add("-J-cp");
                cmdLine.add(Joiner.on(RedSystemProperties.getPathsSeparator()).join(additionalPaths.getClassPaths()));
            }
            cmdLine.add(scriptFile.getAbsolutePath());
            cmdLine.add("-modulename");
            cmdLine.add(moduleName);
            cmdLine.add(RobotRuntimeEnvironment.wrapArgumentIfNeeded(Joiner.on(";").join(additionalPaths.getPythonPaths())));

            RobotRuntimeEnvironment.runExternalProcess(cmdLine, linesHandler);
            if (lines.size() == 1) {
                // there should be a single line with path only
                return Optional.of(new File(lines.get(0).toString()));
            } else {
                final String indent = Strings.repeat(" ", 12);
                final String exception = indent + Joiner.on("\n" + indent).join(lines);
                throw new RobotEnvironmentException(
                        "RED python session problem. Following exception has been thrown by python service:\n"
                                + exception);
            }
        } catch (final IOException e) {
            throw new RobotEnvironmentException("Unable to find path of '" + moduleName + "' module", e);
        }
    }
}
