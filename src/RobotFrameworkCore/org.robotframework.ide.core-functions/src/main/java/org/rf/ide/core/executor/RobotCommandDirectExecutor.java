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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
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

    RobotCommandDirectExecutor(final String interpreterPath) {
        this.interpreterPath = interpreterPath;
    }

    @Override
    public Map<String, Object> getVariables(final String filePath, final List<String> fileArguments) {
        try {
            final String normalizedPath = filePath.replace('\\', '/');

            final File scriptFile = RobotRuntimeEnvironment.copyResourceFile("red_variables.py");
            final List<String> cmdLine = Arrays.asList(interpreterPath, scriptFile.getAbsolutePath(), "-variables",
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
                final List<String> cmdLine = Arrays.asList(interpreterPath, scriptFile.getAbsolutePath(), "-global");
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
            final List<String> cmdLine = Arrays.asList(interpreterPath, scriptFile.getAbsolutePath(), "-names");
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
            final List<String> cmdLine = Arrays.asList(interpreterPath, scriptFile.getAbsolutePath(), "-path",
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
            RobotRuntimeEnvironment.runExternalProcess(Arrays.asList(interpreterPath, "-m", "robot.run", "--version"),
                    linesHandler);
            final String version = versionOutput.toString();
            return version.startsWith("Robot Framework") ? version.trim() : null;
        } catch (final IOException e) {
            return null;
        }
    }

    @Override
    public String getRunModulePath() {
        final StringBuilder output = new StringBuilder();
        final ILineHandler linesHandler = new ILineHandler() {
            @Override
            public void processLine(final String line) {
                output.append(line);
            }
        };
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyResourceFile("red_modules.py");
            final List<String> cmdLine = Arrays.asList(interpreterPath, scriptFile.getAbsolutePath(), "-runmodulepath");

            RobotRuntimeEnvironment.runExternalProcess(cmdLine, linesHandler);
            final String fileAsString = output.toString();

            for (final File file : new File(fileAsString).getParentFile().listFiles()) {
                if (file.getName().equals("run.py")) {
                    return file.getAbsolutePath();
                }
            }
            throw new IllegalArgumentException("Unable to find robot.run module");
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void createLibdocForStdLibrary(final String resultFilePath, final String libName, final String libPath) {
        final List<String> cmdLine = Arrays.asList(interpreterPath, "-m", "robot.libdoc", "-f", "XML", libName,
                resultFilePath);
        runLibdoc(libName, cmdLine);
    }

    @Override
    public void createLibdocForPythonLibrary(final String resultFilePath, final String libName, final String libPath) {
        final List<String> cmdLine = Arrays.asList(interpreterPath, "-m", "robot.libdoc", "-f", "XML", "-P", libPath,
                libName, resultFilePath);
        runLibdoc(libName, cmdLine);
    }

    @Override
    public void createLibdocForJavaLibrary(final String resultFilePath, final String libName, final String libPath) {
        final String cpSeparator = RobotRuntimeEnvironment.isWindows() ? ";" : ":";

        final List<String> wholeClasspath = newArrayList(".", libPath);
        final String sysPath = System.getenv("CLASSPATH");
        if (sysPath != null && !sysPath.isEmpty()) {
            wholeClasspath.add(sysPath);
        }

        final String classPath = "\"" + Joiner.on(cpSeparator).join(wholeClasspath) + "\"";

        final List<String> cmdLine = Arrays.asList(interpreterPath, "-J-cp", classPath, "-m", "robot.libdoc", "-f",
                "XML", libName, resultFilePath);
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
            final List<String> cmdLine = Arrays.asList(interpreterPath, scriptFile.getAbsolutePath(), "-pythonpath");
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
    public Optional<File> getModulePath(final String moduleName) {
        final StringBuilder pathOutput = new StringBuilder();
        final ILineHandler linesHandler = new ILineHandler() {

            @Override
            public void processLine(final String line) {
                pathOutput.append(line);
            }
        };
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyResourceFile("red_modules.py");
            final List<String> cmdLine = Arrays.asList(interpreterPath, scriptFile.getAbsolutePath(), "-modulepath",
                    moduleName);

            RobotRuntimeEnvironment.runExternalProcess(cmdLine, linesHandler);
            return Optional.of(new File(pathOutput.toString()));
        } catch (final IOException e) {
            throw new RobotEnvironmentException("Unable to find path of '" + moduleName + "' module", e);
        }
    }
}
