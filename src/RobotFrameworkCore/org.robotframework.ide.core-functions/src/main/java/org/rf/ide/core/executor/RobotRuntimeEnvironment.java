/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static com.google.common.collect.Lists.newArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.rf.ide.core.execution.ExecutionElementsParser;
import org.rf.ide.core.execution.IExecutionHandler;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;


@SuppressWarnings({ "PMD.GodClass", "PMD.TooManyMethods" })
public class RobotRuntimeEnvironment {

    private static Path temporaryDirectory = null;

    private final File location;

    private String version;


    static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }

    public static void addProcessListener(final PythonProcessListener listener) {
        PythonInterpretersCommandExecutors.getInstance().addProcessListener(listener);
    }

    public static void removeProcessListener(final PythonProcessListener listener) {
        PythonInterpretersCommandExecutors.getInstance().removeProcessListener(listener);
    }

    static int runExternalProcess(final List<String> command,
            final ILineHandler linesHandler) throws IOException {
        try {
            final Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true).start();

            final InputStream inputStream = process.getInputStream();
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream));
            String line;
            while((line = reader.readLine()) != null) {
                linesHandler.processLine(line);
            }
            return process.waitFor();
        } catch (final InterruptedException e) {
            throw new IOException(e);
        }
    }


    /**
     * Locates directories in which python interpreters pointed in environment
     * path are located. Uses where command in Windows and which command under
     * Unix.
     * 
     * @return Directories where python interpreters are installed or empty list
     *         if there is no python at all.
     */
    public static List<PythonInstallationDirectory> whereArePythonInterpreters() {
        final List<PythonInstallationDirectory> paths = new ArrayList<>();

        for (final SuiteExecutor interpreter : EnumSet
                .allOf(SuiteExecutor.class)) {
            paths.addAll(whereIsPythonInterpreter(interpreter));
        }
        return paths;
    }


    private static Collection<PythonInstallationDirectory> whereIsPythonInterpreter(
            final SuiteExecutor interpreter) {
        final List<String> paths = new ArrayList<>();
        final ILineHandler linesProcessor = new ILineHandler(){

            @Override
            public void processLine(final String line) {
                paths.add(line);
            }
        };
        try {
            final String cmd = isWindows() ? "where" : "which";
            final int returnCode = runExternalProcess(
                    Arrays.asList(cmd, interpreter.executableName()),
                    linesProcessor);
            if (returnCode == 0) {
                final List<PythonInstallationDirectory> installationDirectories = new ArrayList<>();

                for (final String path : paths) {
                    final URI dirUri = new File(path).getParentFile().toURI();
                    installationDirectories
                            .add(new PythonInstallationDirectory(dirUri,
                                    interpreter));
                }
                return installationDirectories;
            } else {
                return new ArrayList<>();
            }
        } catch (final IOException e) {
            return new ArrayList<>();
        }
    }


    /**
     * Checks if given location is a directory which contains python
     * interpreter. The {@link IllegalArgumentException} exception is thrown if
     * given location does not contain python executable. Otherwise
     * {@link PythonInstallationDirectory} instance (copy of location, but with
     * other type) is returned.
     * 
     * @param location
     *            Location to check
     * @return the same location given as {@link File} subtype
     * @throws IllegalArgumentException
     *             thrown when given location is not a directory or does not
     *             contain python interpreter executables.
     */
    private static PythonInstallationDirectory checkPythonInstallationDir(
            final File location) throws IllegalArgumentException {
        if (!location.isDirectory()) {
            throw new IllegalArgumentException("The location "
                    + location.getAbsolutePath() + " is not a directory.");
        }
        for (final File file : location.listFiles()) {
            final String fileName = file.getName();
            if (file.isFile()
                    && (fileName.equals("python") || fileName
                            .equals("python.exe"))) {
                return new PythonInstallationDirectory(location.toURI(),
                        SuiteExecutor.Python);
            } else if (file.isFile()
                    && (fileName.equals("jython") || fileName
                            .equals("jython.exe"))) {
                return new PythonInstallationDirectory(location.toURI(),
                        SuiteExecutor.Jython);
            } else if (file.isFile()
                    && (fileName.equals("ipy") || fileName.equals("ipy.exe"))) {
                return new PythonInstallationDirectory(location.toURI(),
                        SuiteExecutor.IronPython);
            } else if (file.isFile()
                    && (fileName.equals("pypy") || fileName.equals("pypy.exe"))) {
                return new PythonInstallationDirectory(location.toURI(),
                        SuiteExecutor.PyPy);
            }
        }
        throw new IllegalArgumentException("The location: "
                + location.getAbsolutePath()
                + " does not seem to be a valid python installation directory");
    }


    /**
     * Gets robot framework version.
     * 
     * @param pythonLocation
     * @return Robot version as returned by robot
     */
    private static String getRobotFrameworkVersion(
            final PythonInstallationDirectory pythonLocation) {
        final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                .getRobotCommandExecutor(pythonLocation);
        return executor.getRobotVersion();
    }


    private static String getRunModulePath(
            final PythonInstallationDirectory pythonLocation) {
        final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                        .getRobotCommandExecutor(pythonLocation);
        return executor.getRunModulePath();
    }


    private static String getPythonExecutablePath(
            final PythonInstallationDirectory location) {
        final String pythonExec = location.interpreter.executableName();
        return findFile(location, pythonExec).getAbsolutePath();
    }


    private static File findFile(
            final PythonInstallationDirectory pythonLocation, final String name) {
        for (final File file : pythonLocation.listFiles()) {
            if (name.equals(file.getName())) {
                return file;
            }
        }
        return null;
    }


    static File copyResourceFile(final String filename) throws IOException {
        final Path tempDir = createTemporaryDirectory();
        final File scriptFile = new File(tempDir.toString() + File.separator
                + filename);
        if (!scriptFile.exists()) {
            Files.copy(
                    RobotRuntimeEnvironment.class.getResourceAsStream(filename),
                    scriptFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return scriptFile;
    }


    private static synchronized Path createTemporaryDirectory()
            throws IOException {
        if (temporaryDirectory != null) {
            return temporaryDirectory;
        }
        temporaryDirectory = Files.createTempDirectory("RobotTempDir");
        addRemoveTemporaryDirectoryHook();
        return temporaryDirectory;
    }


    private static void addRemoveTemporaryDirectoryHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(){

            @Override
            public void run() {
                if (temporaryDirectory == null) {
                    return;
                }
                try {
                    Files.walkFileTree(temporaryDirectory, new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                                throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
                                throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private RobotRuntimeEnvironment(final File location, final String version) {
        this.location = location;
        this.version = version;
    }


    public static RobotRuntimeEnvironment create(final String pathToPython) {
        return create(new File(pathToPython));
    }


    public static RobotRuntimeEnvironment create(final File pathToPython) {
        try {
            final PythonInstallationDirectory location = checkPythonInstallationDir(pathToPython);
            return new RobotRuntimeEnvironment(location, getRobotFrameworkVersion(location));
        } catch (final IllegalArgumentException e) {
            return new RobotRuntimeEnvironment(pathToPython, null);
        }
    }


    public boolean isValidPythonInstallation() {
        return location instanceof PythonInstallationDirectory;
    }


    public boolean hasRobotInstalled() {
        return isValidPythonInstallation() && version != null;
    }


    public String getVersion() {
        return version;
    }


    public SuiteExecutor getInterpreter() {
        return location instanceof PythonInstallationDirectory ? ((PythonInstallationDirectory) location).interpreter
                : null;
    }


    public List<File> getModuleSearchPaths() {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.getModulesSearchPaths();
        }
        return newArrayList();
    }


    public Optional<File> getModulePath(final String moduleName) {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.getModulePath(moduleName);
        }
        return Optional.absent();
    }


    public String getVersion(final SuiteExecutor executor) {
        final boolean isSameInterpreter = executor == getInterpreter();
        if (isSameInterpreter) {
            return version;
        } else {
            final List<String> cmdLine = getRunCommandLine(executor);
            cmdLine.add("--version");
            try {
                final StringBuilder versionOutput = new StringBuilder();
                final ILineHandler linesHandler = new ILineHandler(){

                    @Override
                    public void processLine(final String line) {
                        versionOutput.append(line);
                    }
                };
                runExternalProcess(cmdLine, linesHandler);
                final String ver = versionOutput.toString();
                return ver.startsWith("Robot") ? ver
                        + " (using Robot installation from "
                        + location.getAbsolutePath() + ")" : version;
            } catch (final IOException e) {
                return version;
            }
        }
    }


    public File getFile() {
        return location;
    }


    public void installRobotUsingPip(final ILineHandler linesHandler,
            final boolean useStableVersion) throws RobotEnvironmentException {
        if (isValidPythonInstallation()) {
            final String cmd = getPythonExecutablePath((PythonInstallationDirectory) location);
            final List<String> cmdLine = new ArrayList<>();
            cmdLine.addAll(Arrays.asList(cmd, "-m", "pip", "install",
                    "--upgrade"));
            if (!useStableVersion) {
                cmdLine.add("--pre");
            }
            cmdLine.add("robotframework");
            try {
                final int returnCode = runExternalProcess(cmdLine, linesHandler);
                if (returnCode != 0) {
                    throw new RobotEnvironmentException(
                            "Unable to upgrade Robot installation");
                }
                version = getRobotFrameworkVersion((PythonInstallationDirectory) location);
            } catch (final IOException e) {
                throw new RobotEnvironmentException(
                        "Unable to upgrade Robot installation", e);
            }
        }
    }

    public void resetCommandExecutors() {
        if (hasRobotInstalled()) {
            PythonInterpretersCommandExecutors.getInstance().resetExecutorFor((PythonInstallationDirectory) location);
        }
    }

    public void createLibdocForStdLibrary(final String libName, final File file) throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.createLibdocForStdLibrary(file.getAbsolutePath(), libName, "");
        }
    }

    public void createLibdocForStdLibraryForcibly(final String libName, final File file)
            throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                    .getDirectRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.createLibdocForStdLibrary(file.getAbsolutePath(), libName, "");
        }
    }

    public void createLibdocForPythonLibrary(final String libName, final String libPath, final File file)
            throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.createLibdocForPythonLibrary(file.getAbsolutePath(), libName, libPath);
        }
    }

    public void createLibdocForPythonLibraryForcibly(final String libName, final String libPath, final File file)
            throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                    .getDirectRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.createLibdocForPythonLibrary(file.getAbsolutePath(), libName, libPath);
        }
    }

    public void createLibdocForJavaLibrary(final String libName, final String jarPath, final File file)
            throws RobotEnvironmentException {
        if (hasRobotInstalled() && ((PythonInstallationDirectory) location).interpreter == SuiteExecutor.Jython) {
            final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.createLibdocForJavaLibrary(file.getAbsolutePath(), libName, jarPath);
        }
    }

    public void createLibdocForJavaLibraryForcibly(final String libName, final String jarPath, final File file)
            throws RobotEnvironmentException {
        if (hasRobotInstalled() && ((PythonInstallationDirectory) location).interpreter == SuiteExecutor.Jython) {
            final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                    .getDirectRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.createLibdocForJavaLibrary(file.getAbsolutePath(), libName, jarPath);
        }
    }

    public List<String> getStandardLibrariesNames() {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            final List<String> libs = executor.getStandardLibrariesNames();
            // Remote is a library without keywords and libdoc throws
            // exceptions when trying to generate its specification
            libs.remove("Remote");
            return libs;
        } else {
            return new ArrayList<>();
        }
    }


    public File getStandardLibraryPath(final String libraryName) {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            final String pycPath = executor.getStandardLibraryPath(libraryName);
            if (pycPath == null) {
                return null;
            } else if (pycPath.endsWith(".py")) {
                return new File(pycPath);
            } else if (pycPath.endsWith(".pyc")) {
                return new File(pycPath.substring(0, pycPath.length() - 1));
            } else if (pycPath.endsWith("$py.class")) {
                return new File(pycPath.substring(0, pycPath.length() - 9)
                        + ".py");
            } else {
                return null;
            }
        }
        return null;
    }


    /**
     * Return names of python classes contained in module point by argument and
     * all of its submodules. For packages-module __init__.py file path should
     * be provided.
     * 
     * @param moduleLocation
     * @return
     * @throws RobotEnvironmentException
     */
    public List<String> getClassesDefinedInModule(final File moduleLocation)
            throws RobotEnvironmentException {
        // DO NOT split & move to direct/rpc executors since this code may
        // import quite a lot of
        // modules;
        // maybe we could restart xml-rpc server from time to time; then we can
        // consider moving this
        try {
            final File scriptFile = RobotRuntimeEnvironment
                    .copyResourceFile("module_classes_printer.py");

            if (scriptFile != null) {
                final String interpreterPath = location.toPath()
                        .resolve(((PythonInstallationDirectory) location).getInterpreter().executableName())
                        .toAbsolutePath()
                        .toString();

                String modulePath = moduleLocation.getAbsolutePath();
                modulePath = modulePath.contains(" ") ? "\"" + modulePath + "\"" : modulePath;
                final List<String> cmdLine = Arrays.asList(interpreterPath,
                        scriptFile.getAbsolutePath(),
                        modulePath);
                final List<String> output = newArrayList();
                final ILineHandler linesHandler = new ILineHandler(){

                    @Override
                    public void processLine(final String line) {
                        output.add(line);
                    }
                };
                final int result = RobotRuntimeEnvironment.runExternalProcess(
                        cmdLine, linesHandler);
                if (result == 0) {
                    return output;
                } else {
                    throw new RobotEnvironmentException(
                            "Python interpreter returned following errors:\n\n"
                                    + Joiner.on('\n').join(output));
                }
            }
            return newArrayList();
        } catch (final IOException e) {
            return newArrayList();
        }
    }


    public Map<String, Object> getGlobalVariables() {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.getGlobalVariables();
        }
        return new LinkedHashMap<>();
    }


    public Map<String, Object> getVariablesFromFile(final String path, final List<String> args) {
        if (hasRobotInstalled()) {
            final String normalizedPath = path.replace('\\', '/');
            final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.getVariables(normalizedPath, args);
        }
        return new LinkedHashMap<String, Object>();
    }


    public RunCommandLine createCommandLineCall(final SuiteExecutor executor,
            final List<String> classpath, final List<String> pythonpath,
            final List<String> variableFilesPath, final File projectLocation,
            final List<String> suites, final List<String> testCases,
            final String userArguments, final List<String> includedTags,
            final List<String> excludedTags, final boolean isDebugging)
            throws IOException {

        final String debugInfo = isDebugging ? "True" : "False";
        final int port = findFreePort();

        final List<String> cmdLine = new ArrayList<String>(getRunCommandLine(
                executor, classpath, pythonpath, variableFilesPath));

        addTags(cmdLine, includedTags, excludedTags);

        cmdLine.add("--listener");
        cmdLine.add(copyResourceFile("TestRunnerAgent.py").toPath() + ":"
                + port + ":" + debugInfo);

        for (final String suite : suites) {
            cmdLine.add("-s");
            cmdLine.add(suite);
        }
        for (final String testCase : testCases) {
            cmdLine.add("-t");
            cmdLine.add(testCase.trim());
        }
        if (!userArguments.trim().isEmpty()) {
            cmdLine.addAll(ArgumentsConverter
                    .fromJavaArgsToPythonLike(ArgumentsConverter
                            .convertToJavaMainLikeArgs(userArguments)));
        }
        cmdLine.add(projectLocation.getAbsolutePath());
        return new RunCommandLine(cmdLine, port);
    }


    private List<String> getRunCommandLine(final SuiteExecutor executor) {
        final List<String> cmdLine = new ArrayList<String>();
        if (executor == getInterpreter()) {
            cmdLine.add(getPythonExecutablePath((PythonInstallationDirectory) location));
            cmdLine.add("-m");
            cmdLine.add("robot.run");
        } else {
            cmdLine.add(executor.executableName());
            cmdLine.add("\""
                    + getRunModulePath((PythonInstallationDirectory) location)
                    + "\"");
        }
        return cmdLine;
    }


    private List<String> getRunCommandLine(final SuiteExecutor executor,
            final List<String> classpath, final List<String> pythonpath,
            final List<String> variableFilesPath) {
        final List<String> cmdLine = new ArrayList<String>();
        if (executor == getInterpreter()) {
            cmdLine.add(getPythonExecutablePath((PythonInstallationDirectory) location));
            augmentClasspath(cmdLine, executor, classpath);
            cmdLine.add("-m");
            cmdLine.add("robot.run");
        } else {
            cmdLine.add(executor.executableName());
            augmentClasspath(cmdLine, executor, classpath);
            cmdLine.add("\""
                    + getRunModulePath((PythonInstallationDirectory) location)
                    + "\"");
        }
        addPythonpath(cmdLine, pythonpath);
        addVariableFilesPath(cmdLine, variableFilesPath);
        return cmdLine;
    }


    private void augmentClasspath(final List<String> cmdLine,
            final SuiteExecutor executor, final List<String> classpath) {
        if (executor == SuiteExecutor.Jython) {
            final String cpSeparator = isWindows() ? ";" : ":";

            final String sysPath = System.getenv("CLASSPATH");
            final List<String> wholeClasspath = newArrayList();
            if (sysPath != null && !sysPath.isEmpty()) {
                wholeClasspath.add(sysPath);
            }
            wholeClasspath.addAll(classpath);

            final String cpath = "\"" + Joiner.on(cpSeparator).join(wholeClasspath) + "\"";

            cmdLine.add("-J-cp");
            cmdLine.add(cpath.replaceAll("\n", ""));
        }
    }


    private void addPythonpath(final List<String> cmdLine,
            final List<String> pythonpath) {
        if (!pythonpath.isEmpty()) {
            cmdLine.add("-P");
            cmdLine.add(Joiner.on(":").join(pythonpath));
        }
    }


    private void addVariableFilesPath(final List<String> cmdLine,
            final List<String> variableFilesPath) {
        if (!variableFilesPath.isEmpty()) {
            for (final String path : variableFilesPath) {
                cmdLine.add("-V");
                cmdLine.add(path);
            }
        }
    }


    private void addTags(final List<String> cmdLine,
            final List<String> includedTags, final List<String> excludedTags) {
        for (final String tag : includedTags) {
            cmdLine.add("-i");
            cmdLine.add(tag);
        }
        for (final String tag : excludedTags) {
            cmdLine.add("-e");
            cmdLine.add(tag);
        }
    }


    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (final IOException e) {
            return -1;
        }
    }


    public void startTestRunnerAgentHandler(final int port,
            final ILineHandler lineHandler,
            final IExecutionHandler executionHandler) {
        final TestRunnerAgentHandler testRunnerAgentHandler = new TestRunnerAgentHandler(
                port);
        testRunnerAgentHandler.addListener(new MessageLogParser(lineHandler));
        testRunnerAgentHandler.addListener(new ExecutionElementsParser(
                executionHandler));
        final Thread handlerThread = new Thread(testRunnerAgentHandler);
        handlerThread.start();
    }
    
    public RunCommandLine createRunRemoteDebugTempScriptCmd(final int port) {
        try {
            final File scriptFile = RobotRuntimeEnvironment.copyResourceFile("RemoteDebugTempScript.py");
            if (scriptFile != null) {
                final String interpreterPath = location.toPath()
                        .resolve(((PythonInstallationDirectory) location).getInterpreter().executableName())
                        .toAbsolutePath()
                        .toString();
                return new RunCommandLine(Arrays.asList(interpreterPath, scriptFile.getAbsolutePath()), port);
            }
            return new RunCommandLine(new ArrayList<String>(), -1);
        } catch (final IOException e) {
            return new RunCommandLine(new ArrayList<String>(), -1);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, version);
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RobotRuntimeEnvironment other = (RobotRuntimeEnvironment) obj;
        return Objects.equals(location, other.location)
                && Objects.equals(version, other.version);
    }

    @SuppressWarnings("serial")
    public static class RobotEnvironmentException extends RuntimeException {

        public RobotEnvironmentException(final String message) {
            super(message);
        }

        public RobotEnvironmentException(final String message,
                final Throwable cause) {
            super(message, cause);
        }
    }

    @SuppressWarnings("serial")
    public static class PythonInstallationDirectory extends File {

        private final SuiteExecutor interpreter;


        // we dont' want anyone to create those objects; they should only be
        // created
        // when given uri is valid python location
        private PythonInstallationDirectory(final URI uri,
                final SuiteExecutor interpreter) {
            super(uri);
            this.interpreter = interpreter;
        }


        SuiteExecutor getInterpreter() {
            return interpreter;
        }
    }

    public static class RunCommandLine {

        private final List<String> commandLine;
        private final int port;


        RunCommandLine(final List<String> commandLine, final int port) {
            this.commandLine = new ArrayList<String>(commandLine);
            this.port = port;
        }


        public String[] getCommandLine() {
            return commandLine.toArray(new String[0]);
        }


        public int getPort() {
            return port;
        }
    }
}
