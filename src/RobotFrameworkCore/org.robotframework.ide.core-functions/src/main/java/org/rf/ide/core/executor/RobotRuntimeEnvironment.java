/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.Optional;
import java.util.function.Consumer;

import org.rf.ide.core.rflint.RfLintRule;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;

@SuppressWarnings({ "PMD.GodClass", "PMD.TooManyMethods" })
public class RobotRuntimeEnvironment {

    private static Path temporaryDirectory = null;

    private final RobotCommandsExecutors executors;

    private final File location;

    private final String version;

    public static void addProcessListener(final PythonProcessListener listener) {
        PythonInterpretersCommandExecutors.getInstance().addProcessListener(listener);
    }

    public static void removeProcessListener(final PythonProcessListener listener) {
        PythonInterpretersCommandExecutors.getInstance().removeProcessListener(listener);
    }

    public static int runExternalProcess(final List<String> command, final Consumer<String> lineHandler)
            throws IOException {
        try {
            final Process process = new ProcessBuilder(command).redirectErrorStream(true).start();

            final InputStream inputStream = process.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                lineHandler.accept(line);
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

        for (final SuiteExecutor interpreter : EnumSet.allOf(SuiteExecutor.class)) {
            paths.addAll(whereIsPythonInterpreter(interpreter));
        }
        return paths;
    }

    public static String getVersion(final SuiteExecutor interpreter) throws RobotEnvironmentException {
        final Collection<PythonInstallationDirectory> interpreterLocations = whereIsPythonInterpreter(interpreter);
        final Optional<PythonInstallationDirectory> installationDirectory = interpreterLocations.stream().findFirst();
        if (!installationDirectory.isPresent()) {
            throw new RobotEnvironmentException(
                    "There is no " + interpreter.name() + " interpreter in system PATH environment variable");
        }
        final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                .getDirectRobotCommandExecutor(installationDirectory.get());
        return exactVersion(interpreter, executor.getRobotVersion());
    }

    private static String exactVersion(final SuiteExecutor interpreter, final String version) {
        return version != null && interpreter == SuiteExecutor.IronPython64
                ? version.replaceAll("IronPython", "IronPython x64")
                : version;
    }

    private static Collection<PythonInstallationDirectory> whereIsPythonInterpreter(final SuiteExecutor interpreter) {
        final List<String> paths = new ArrayList<>();
        try {
            final String cmd = RedSystemProperties.isWindowsPlatform() ? "where" : "which";
            final int exitCode = runExternalProcess(Arrays.asList(cmd, interpreter.executableName()),
                    line -> paths.add(line));
            if (exitCode == 0) {
                final List<PythonInstallationDirectory> installationDirectories = new ArrayList<>();

                for (final String path : paths) {
                    final URI dirUri = new File(path).getParentFile().toURI();
                    installationDirectories.add(new PythonInstallationDirectory(dirUri, interpreter));
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
    private static PythonInstallationDirectory checkPythonInstallationDir(final File location)
            throws IllegalArgumentException {
        if (!location.isDirectory()) {
            throw new IllegalArgumentException("The location " + location.getAbsolutePath() + " is not a directory.");
        }
        final List<PythonInstallationDirectory> installations = possibleInstallationsFor(location);
        if (installations.isEmpty()) {
            throw new IllegalArgumentException("The location: " + location.getAbsolutePath()
                    + " does not seem to be a valid python installation directory");
        }
        return installations.get(0);
    }

    public static List<PythonInstallationDirectory> possibleInstallationsFor(final File location) {
        final List<PythonInstallationDirectory> installations = new ArrayList<>();

        if (!location.exists()) {
            return installations;
        }
        for (final File file : location.listFiles()) {
            final String fileName = file.getName();
            if (file.isFile() && (fileName.equals("python") || fileName.equals("python.exe"))) {
                installations.add(new PythonInstallationDirectory(location.toURI(), SuiteExecutor.Python));
            }
            if (file.isFile() && (fileName.equals("jython") || fileName.equals("jython.exe"))) {
                installations.add(new PythonInstallationDirectory(location.toURI(), SuiteExecutor.Jython));
            }
            if (file.isFile() && (fileName.equals("ipy") || fileName.equals("ipy.exe"))) {
                installations.add(new PythonInstallationDirectory(location.toURI(), SuiteExecutor.IronPython));
            }
            if (file.isFile() && (fileName.equals("ipy64") || fileName.equals("ipy64.exe"))) {
                installations.add(new PythonInstallationDirectory(location.toURI(), SuiteExecutor.IronPython64));
            }
            if (file.isFile() && (fileName.equals("pypy") || fileName.equals("pypy.exe"))) {
                installations.add(new PythonInstallationDirectory(location.toURI(), SuiteExecutor.PyPy));
            }
        }
        return installations;
    }

    /**
     * Gets robot framework version.
     *
     * @param pythonLocation
     * @return Robot version as returned by robot
     */
    private static String getRobotFrameworkVersion(final PythonInstallationDirectory pythonLocation) {
        final RobotCommandExecutor executor = PythonInterpretersCommandExecutors.getInstance()
                .getRobotCommandExecutor(pythonLocation);
        return exactVersion(pythonLocation.getInterpreter(), executor.getRobotVersion());
    }

    public String getPythonExecutablePath() {
        final PythonInstallationDirectory pyLocation = (PythonInstallationDirectory) location;
        final String pythonExec = pyLocation.interpreter.executableName();
        return findFile(pyLocation, pythonExec).getAbsolutePath();
    }

    private static File findFile(final PythonInstallationDirectory pythonLocation, final String name) {
        for (final File file : pythonLocation.listFiles()) {
            if (name.equals(file.getName())) {
                return file;
            }
        }
        return null;
    }

    public static File createTemporaryFile(final String filename) throws IOException {
        final Path tempDir = createTemporaryDirectory();
        final File tempFile = new File(tempDir.toString() + File.separator + filename);
        tempFile.delete();
        tempFile.createNewFile();
        return tempFile;
    }

    public static File copyScriptFile(final String filename) throws IOException {
        final Path tempDir = createTemporaryDirectory();
        final File scriptFile = new File(tempDir.toString() + File.separator + filename);
        if (!scriptFile.exists()) {
            Files.copy(getScriptFileAsStream(filename), scriptFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return scriptFile;
    }

    public static InputStream getScriptFileAsStream(final String filename) throws IOException {
        return RobotRuntimeEnvironment.class.getResourceAsStream("/scripts/" + filename);
    }

    static synchronized Path createTemporaryDirectory() throws IOException {
        if (temporaryDirectory != null) {
            return temporaryDirectory;
        }
        temporaryDirectory = Files.createTempDirectory("RobotTempDir");
        addRemoveTemporaryDirectoryHook();
        return temporaryDirectory;
    }

    private static void addRemoveTemporaryDirectoryHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {

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
        this(PythonInterpretersCommandExecutors.getInstance(), location, version);
    }

    @VisibleForTesting
    RobotRuntimeEnvironment(final RobotCommandsExecutors executors, final File location, final String version) {
        this.executors = executors;
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

    public static RobotRuntimeEnvironment create(final String pathToPython, final SuiteExecutor interpreter) {
        return create(new File(pathToPython), interpreter);
    }

    public static RobotRuntimeEnvironment create(final File pathToPython, final SuiteExecutor interpreter) {
        try {
            final PythonInstallationDirectory location = checkPythonInstallationDir(pathToPython);
            final PythonInstallationDirectory correctedLocation = new PythonInstallationDirectory(location.toURI(),
                    interpreter);
            return new RobotRuntimeEnvironment(correctedLocation, getRobotFrameworkVersion(correctedLocation));
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
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.getModulesSearchPaths().stream().map(this::tryToCanonical).collect(toList());
        }
        return new ArrayList<>();
    }

    public Optional<File> getModulePath(final String moduleName, final EnvironmentSearchPaths additionalPaths) {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.getModulePath(moduleName, additionalPaths).map(this::tryToCanonical);
        }
        return Optional.empty();
    }

    private File tryToCanonical(final File file) {
        try {
            return file.getCanonicalFile();
        } catch (final IOException e) {
            return file;
        }
    }

    public File getFile() {
        return location;
    }

    public void resetCommandExecutors() {
        if (hasRobotInstalled()) {
            executors.resetExecutorFor((PythonInstallationDirectory) location);
        }
    }

    public void createLibdocForStdLibrary(final String libName, final File outputFile)
            throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.createLibdocForStdLibrary(outputFile.getAbsolutePath(), libName, "");
        }
    }

    public void createLibdocForStdLibraryForcibly(final String libName, final File outputFile)
            throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getDirectRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.createLibdocForStdLibrary(outputFile.getAbsolutePath(), libName, "");
        }
    }

    public void createLibdocForThirdPartyLibrary(final String libName, final String libPath,
            final EnvironmentSearchPaths additionalPaths, final File outputFile) throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.createLibdocForThirdPartyLibrary(outputFile.getAbsolutePath(), libName, libPath, additionalPaths);
        }
    }

    public void createLibdocForThirdPartyLibraryForcibly(final String libName, final String libPath,
            final EnvironmentSearchPaths additionalPaths, final File outputFile) throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getDirectRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.createLibdocForThirdPartyLibrary(outputFile.getAbsolutePath(), libName, libPath, additionalPaths);
        }
    }

    public List<String> getStandardLibrariesNames() {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
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
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            final String pycPath = executor.getStandardLibraryPath(libraryName);
            if (pycPath == null) {
                return null;
            } else if (pycPath.endsWith(".py")) {
                return new File(pycPath);
            } else if (pycPath.endsWith(".pyc")) {
                return new File(pycPath.substring(0, pycPath.length() - 1));
            } else if (pycPath.endsWith("$py.class")) {
                return new File(pycPath.substring(0, pycPath.length() - 9) + ".py");
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
     *            Module location
     * @return list of class names or empty list
     * @throws RobotEnvironmentException
     */
    public List<String> getClassesFromModule(final File moduleLocation, final EnvironmentSearchPaths additionalPaths)
            throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.getClassesFromModule(moduleLocation, additionalPaths);
        }
        return new ArrayList<>();
    }

    public Map<String, Object> getGlobalVariables() {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.getGlobalVariables();
        }
        return new LinkedHashMap<>();
    }

    public Map<String, Object> getVariablesFromFile(final String path, final List<String> args) {
        if (hasRobotInstalled()) {
            final String normalizedPath = path.replace('\\', '/');
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            return executor.getVariables(normalizedPath, args);
        }
        return new LinkedHashMap<>();
    }

    public void runRfLint(final String host, final int port, final File filepath, final List<RfLintRule> rules,
            final List<String> rulesFiles) {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.runRfLint(host, port, filepath, rules, rulesFiles);
        }
    }

    /**
     * Start library auto discovering with robot dryrun
     *
     * @param port
     *            Port number for communication with AgentConnectionServer
     * @param dataSource
     *            Test case file with unknown library imports
     * @param projectLocation
     *            Project file
     * @param recursiveInVirtualenv
     *            Virtualenv recursive library source lookup switch
     * @throws RobotEnvironmentException
     */
    public void startLibraryAutoDiscovering(final int port, final File dataSource, final File projectLocation,
            final boolean recursiveInVirtualenv) throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.startLibraryAutoDiscovering(port, dataSource, projectLocation, recursiveInVirtualenv);
        }
    }

    /**
     * Start keyword auto discovering with robot dryrun
     *
     * @param port
     *            Port number for communication with AgentConnectionServer
     * @param dataSource
     *            Test case file with known library imports
     * @param additionalPaths
     *            Additional pythonPaths and classPaths
     * @throws RobotEnvironmentException
     */
    public void startKeywordAutoDiscovering(final int port, final File dataSource,
            final EnvironmentSearchPaths additionalPaths) throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.startKeywordAutoDiscovering(port, dataSource, additionalPaths);
        }
    }

    public void stopAutoDiscovering() throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final RobotCommandExecutor executor = executors
                    .getRobotCommandExecutor((PythonInstallationDirectory) location);
            executor.stopAutoDiscovering();
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
        return Objects.equals(location, other.location) && Objects.equals(version, other.version);
    }

    @SuppressWarnings("serial")
    public static class RobotEnvironmentException extends RuntimeException {

        public RobotEnvironmentException(final String message) {
            super(message);
        }

        public RobotEnvironmentException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    @SuppressWarnings("serial")
    public static class RobotEnvironmentDetailedException extends RobotEnvironmentException {

        private final String details;

        public RobotEnvironmentDetailedException(final String details, final String reason) {
            super(reason);
            this.details = details;
        }

        public RobotEnvironmentDetailedException(final String details, final String reason, final Throwable cause) {
            super(reason, cause);
            this.details = details;
        }

        public String getReason() {
            return getMessage();
        }

        public String getDetails() {
            return details;
        }
    }

    @SuppressWarnings("serial")
    public static class PythonInstallationDirectory extends File {

        private final SuiteExecutor interpreter;

        // we dont' want anyone to create those objects; they should only be
        // created
        // when given uri is valid python location
        PythonInstallationDirectory(final URI uri, final SuiteExecutor interpreter) {
            super(uri);
            this.interpreter = interpreter;
        }

        public SuiteExecutor getInterpreter() {
            return interpreter;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof PythonInstallationDirectory) {
                final PythonInstallationDirectory that = (PythonInstallationDirectory) obj;
                return super.equals(obj) && this.interpreter == that.interpreter;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + ((interpreter == null) ? 0 : interpreter.hashCode());
        }
    }
}
