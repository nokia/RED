package org.robotframework.ide.core.executor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class RobotRuntimeEnvironment {

    private static Path temporaryDirectory = null;

    private final File location;

    private String version;

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }

    private static int runExternalProcess(final List<String> command, final ILineHandler linesHandler)
            throws IOException {
        try {
            final Process process = new ProcessBuilder(command).start();

            final InputStream inputStream = process.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
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

        for (final SuiteExecutor interpreter : EnumSet.allOf(SuiteExecutor.class)) {
            paths.addAll(whereIsPythonInterpreter(interpreter));
        }
        return paths;
    }

    private static Collection<PythonInstallationDirectory> whereIsPythonInterpreter(final SuiteExecutor interpreter) {
        final List<String> paths = new ArrayList<>();
        final ILineHandler linesProcessor = new ILineHandler() {
            @Override
            public void processLine(final String line) {
                paths.add(line);
            }
        };
        try {
            final String cmd = isWindows() ? "where" : "which";
            final int returnCode = runExternalProcess(Arrays.asList(cmd, interpreter.executableName()), linesProcessor);
            if (returnCode == 0) {
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
        for (final File file : location.listFiles()) {
            final String fileName = file.getName();
            if (file.isFile() && (fileName.equals("python") || fileName.equals("python.exe"))) {
                return new PythonInstallationDirectory(location.toURI(), SuiteExecutor.Python);
            } else if (file.isFile() && (fileName.equals("jython") || fileName.equals("jython.exe"))) {
                return new PythonInstallationDirectory(location.toURI(), SuiteExecutor.Jython);
            } else if (file.isFile() && (fileName.equals("ipy") || fileName.equals("ipy.exe"))) {
                return new PythonInstallationDirectory(location.toURI(), SuiteExecutor.IronPython);
            } else if (file.isFile() && (fileName.equals("pypy") || fileName.equals("pypy.exe"))) {
                return new PythonInstallationDirectory(location.toURI(), SuiteExecutor.PyPy);
            }
        }
        throw new IllegalArgumentException("The location: " + location.getAbsolutePath()
                + " does not seem to be a valid python installation directory");
    }


    /**
     * Gets robot framework version as returned by following call:
     *      [interpreter_exec] -m robot.run --version
     *   e.g.
     *      python -m robot.run --version
     *      pypy -m robot.run --version 
     *      
     * @param pythonLocation
     * @return Robot version as returned by robot
     */
    private static String getRobotFrameworkVersion(final PythonInstallationDirectory pythonLocation) {
        final StringBuilder versionOutput = new StringBuilder();
        final ILineHandler linesHandler = new ILineHandler() {
            @Override
            public void processLine(final String line) {
                versionOutput.append(line);
            }
        };
        try {
            runExternalProcess(Arrays.asList(getPythonExecutablePath(pythonLocation), "-m", "robot.run", "--version"),
                    linesHandler);
            final String output = versionOutput.toString();
            return output.startsWith("Robot Framework") ? output.trim() : null;
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String getRunModulePath(final PythonInstallationDirectory pythonLocation) {
        final StringBuilder versionOutput = new StringBuilder();
        final ILineHandler linesHandler = new ILineHandler() {
            @Override
            public void processLine(final String line) {
                versionOutput.append(line);
            }
        };
        try {
            runExternalProcess(
                    Arrays.asList(getPythonExecutablePath(pythonLocation), "-c", "import robot;print(robot.__file__)"),
                    linesHandler);
            final String output = versionOutput.toString();
            for (final File file : new File(output.trim()).getParentFile().listFiles()) {
                if (file.getName().equals("run.py")) {
                    return file.getAbsolutePath();
                }
            }
            throw new IllegalArgumentException("Unable to find robot.run module");
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String getPythonExecutablePath(final PythonInstallationDirectory location) {
        final String pythonExec = location.interpreter.executableName();
        return findFile(location, pythonExec).getAbsolutePath();
    }

    private static File findFile(final PythonInstallationDirectory pythonLocation, final String name) {
        for (final File file : pythonLocation.listFiles()) {
            if (name.equals(file.getName())) {
                return file;
            }
        }
        return null;
    }

    private static File copyResourceFile(final String filename) throws IOException {
        final Path tempDir = createTemporaryDirectory();
        final File scriptFile = new File(tempDir.toString() + File.separator + filename);
        Files.copy(RobotRuntimeEnvironment.class.getResourceAsStream(filename), scriptFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        return scriptFile;
    }

    private static synchronized Path createTemporaryDirectory() throws IOException {
        if (temporaryDirectory != null) {
            return temporaryDirectory;
        }
        temporaryDirectory = Files.createTempDirectory("RobotTempDir");
        temporaryDirectory.toFile().deleteOnExit();
        return temporaryDirectory;
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

    public String getVersion(final SuiteExecutor executor) {
        final boolean isSameInterpreter = executor == getInterpreter();
        if (isSameInterpreter) {
            return version;
        } else {
            final List<String> cmdLine = getRunCommandLine(executor);
            cmdLine.add("--version");
            try {
                final StringBuilder versionOutput = new StringBuilder();
                final ILineHandler linesHandler = new ILineHandler() {
                    @Override
                    public void processLine(final String line) {
                        versionOutput.append(line);
                    }
                };

                runExternalProcess(cmdLine, linesHandler);
                final String ver = versionOutput.toString();
                return ver.startsWith("Robot") ? ver + " (using Robot installation from " + location.getAbsolutePath()
                        + ")" : version;
            } catch (final IOException e) {
                return version;
            }
        }
    }

    public File getFile() {
        return location;
    }

    public void installRobotUsingPip(final ILineHandler linesHandler, final boolean useStableVersion)
            throws RobotEnvironmentException {
        if (isValidPythonInstallation()) {
            final String cmd = getPythonExecutablePath((PythonInstallationDirectory) location);
            final List<String> cmdLine = new ArrayList<>();
            cmdLine.addAll(Arrays.asList(cmd, "-m", "pip", "install", "--upgrade"));
            if (!useStableVersion) {
                cmdLine.add("--pre");
            }
            cmdLine.add("robotframework");
            try {
                final int returnCode = runExternalProcess(cmdLine, linesHandler);
                if (returnCode != 0) {
                    throw new RobotEnvironmentException("Unable to upgrade Robot installation");
                }
                version = getRobotFrameworkVersion((PythonInstallationDirectory) location);
            } catch (final IOException e) {
                throw new RobotEnvironmentException("Unable to upgrade Robot installation", e);
            }
        }
    }

    public void createLibdocForStdLibrary(final String libName, final File file) {
        if (hasRobotInstalled()) {
            final String cmd = getPythonExecutablePath((PythonInstallationDirectory) location);
            final List<String> cmdLine = Arrays.asList(cmd, "-m", "robot.libdoc", "-f", "XML", libName,
                    file.getAbsolutePath());
            final ILineHandler linesHandler = new ILineHandler() {
                @Override
                public void processLine(final String line) {
                    // nothing to do
                }
            };

            try {
                runExternalProcess(cmdLine, linesHandler);
            } catch (final IOException e) {
                return;
            }
        }
    }

    public List<String> getStandardLibrariesNames() {
        if (hasRobotInstalled()) {
            try {
                final File scriptFile = copyResourceFile("StdLibrariesReader.py");

                final String cmd = getPythonExecutablePath((PythonInstallationDirectory) location);
                final List<String> cmdLine = Arrays.asList(cmd, scriptFile.getAbsolutePath());
                final List<String> stdLibs = new ArrayList<>();
                final ILineHandler linesHandler = new ILineHandler() {
                    @Override
                    public void processLine(final String line) {
                        final String libName = line.trim();
                        // Remote is a library without keywords and libdoc
                        // throws
                        // exceptions when trying to generate its specification
                        if (!libName.equals("Remote")) {
                            stdLibs.add(libName);
                        }
                    }
                };

                runExternalProcess(cmdLine, linesHandler);
                return stdLibs;
            } catch (final IOException e) {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    public File getStandardLibraryPath(final String libraryName) {
        if (hasRobotInstalled()) {
            final String cmd = getPythonExecutablePath((PythonInstallationDirectory) location);
            final List<String> cmdLine = Arrays.asList(cmd, "-c", "import robot.libraries." + libraryName
                    + ";print(robot.libraries." + libraryName + ".__file__)");
            final StringBuilder path = new StringBuilder();
            final ILineHandler linesHandler = new ILineHandler() {
                @Override
                public void processLine(final String line) {
                    path.append(line);
                }
            };

            try {
                runExternalProcess(cmdLine, linesHandler);
                final String pycPath = path.toString().trim();
                if (pycPath.endsWith(".pyc")) {
                    return new File(pycPath.substring(0, pycPath.length() - 1));
                }
                return null;
            } catch (final IOException e) {
                return null;
            }
        }
        return null;
    }

    public RunCommandLine createCommandLineCall(final SuiteExecutor executor, final File projectLocation,
            final List<String> suites, final String userArguments, final boolean isDebugging) throws IOException {
        final String debugInfo = isDebugging ? "True" : "False";
        final int port = findFreePort();

        final List<String> cmdLine = new ArrayList<String>(getRunCommandLine(executor));
        cmdLine.add("--listener");
        cmdLine.add(copyResourceFile("TestRunnerAgent.py").toPath() + ":" + port + ":" + debugInfo);

        for (final String suite : suites) {
            cmdLine.add("--suite");
            cmdLine.add(suite);
        }
        if (!userArguments.trim().isEmpty()) {
            cmdLine.addAll(ArgumentsConverter.fromJavaArgsToPythonLike(ArgumentsConverter
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
            cmdLine.add("\"" + getRunModulePath((PythonInstallationDirectory) location) + "\"");
        }
        return cmdLine;
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (final IOException e) {
            return -1;
        }
    }

    public void startTestRunnerAgentHandler(final int port, final ILineHandler lineHandler) {
        final TestRunnerAgentHandler testRunnerAgentHandler = new TestRunnerAgentHandler(port);
        testRunnerAgentHandler.addListener(new MessageLogParser(lineHandler));
        final Thread handlerThread = new Thread(testRunnerAgentHandler);
        handlerThread.start();
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

    public static class RobotEnvironmentException extends Exception {

        public RobotEnvironmentException(final String message) {
            super(message);
        }

        public RobotEnvironmentException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    public static class PythonInstallationDirectory extends File {
        
        private final SuiteExecutor interpreter;

        // we dont' want anyone to create those objects; they should only be created
        // when given uri is valid python location
        private PythonInstallationDirectory(final URI uri, final SuiteExecutor interpreter) {
            super(uri);
            this.interpreter = interpreter;
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
