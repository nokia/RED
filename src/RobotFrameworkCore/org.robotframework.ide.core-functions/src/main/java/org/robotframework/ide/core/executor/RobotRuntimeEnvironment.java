package org.robotframework.ide.core.executor;

import static com.google.common.collect.Lists.newArrayList;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.codehaus.jackson.map.ObjectMapper;
import org.robotframework.ide.core.execution.ExecutionElementsParser;
import org.robotframework.ide.core.execution.IExecutionHandler;

import com.google.common.base.Joiner;


public class RobotRuntimeEnvironment {

    private static Path temporaryDirectory = null;

    private final File location;

    private String version;

    private ObjectMapper mapper;


    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }


    private static int runExternalProcess(final List<String> command,
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
     * Gets robot framework version as returned by following call:
     * [interpreter_exec] -m robot.run --version e.g. python -m robot.run
     * --version pypy -m robot.run --version
     * 
     * @param pythonLocation
     * @return Robot version as returned by robot
     */
    private static String getRobotFrameworkVersion(
            final PythonInstallationDirectory pythonLocation) {
        final StringBuilder versionOutput = new StringBuilder();
        final ILineHandler linesHandler = new ILineHandler(){

            @Override
            public void processLine(final String line) {
                versionOutput.append(line);
            }
        };
        try {
            runExternalProcess(Arrays.asList(
                    getPythonExecutablePath(pythonLocation), "-m", "robot.run",
                    "--version"), linesHandler);
            final String output = versionOutput.toString();
            return output.startsWith("Robot Framework") ? output.trim() : null;
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }


    private static String getRunModulePath(
            final PythonInstallationDirectory pythonLocation) {
        final StringBuilder versionOutput = new StringBuilder();
        final ILineHandler linesHandler = new ILineHandler(){

            @Override
            public void processLine(final String line) {
                versionOutput.append(line);
            }
        };
        try {
            runExternalProcess(Arrays.asList(
                    getPythonExecutablePath(pythonLocation), "-c",
                    "import robot;print(robot.__file__)"), linesHandler);
            final String output = versionOutput.toString();
            for (final File file : new File(output.trim()).getParentFile()
                    .listFiles()) {
                if (file.getName().equals("run.py")) {
                    return file.getAbsolutePath();
                }
            }
            throw new IllegalArgumentException(
                    "Unable to find robot.run module");
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
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


    private static File copyResourceFile(final String filename)
            throws IOException {
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

                if (temporaryDirectory != null) {
                    final File[] files = new File(temporaryDirectory.toString())
                            .listFiles();
                    try {
                        for (int i = 0; i < files.length; i++) {
                            Files.delete(files[i].toPath());
                        }
                        Files.delete(temporaryDirectory);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
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
            return new RobotRuntimeEnvironment(location,
                    getRobotFrameworkVersion(location));
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


    public void createLibdocForStdLibrary(final String libName, final File file)
            throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final String cmd = getPythonExecutablePath((PythonInstallationDirectory) location);
            final List<String> cmdLine = Arrays.asList(cmd, "-m",
                    "robot.libdoc", "-f", "XML", libName,
                    file.getAbsolutePath());

            runLibdoc(libName, cmdLine);
        }
    }


    public void createLibdocForPythonLibrary(final String libName,
            final String libPath, final File file)
            throws RobotEnvironmentException {
        if (hasRobotInstalled()) {
            final String cmd = getPythonExecutablePath((PythonInstallationDirectory) location);

            final List<String> cmdLine = Arrays.asList(cmd, "-m",
                    "robot.libdoc", "-f", "XML", "-P", libPath, libName,
                    file.getAbsolutePath());

            runLibdoc(libName, cmdLine);
        }
    }


    public void createLibdocForJavaLibrary(final String libName,
            final String jarPath, final File file)
            throws RobotEnvironmentException {
        if (hasRobotInstalled()
                && ((PythonInstallationDirectory) location).interpreter == SuiteExecutor.Jython) {
            final String cmd = getPythonExecutablePath((PythonInstallationDirectory) location);

            final String cpSeparator = isWindows() ? ";" : ":";
            final String classPath = "\""
                    + Joiner.on(cpSeparator).join(Arrays.asList(".", jarPath))
                    + "\"";

            final List<String> cmdLine = Arrays.asList(cmd, "-J-cp", classPath,
                    "-m", "robot.libdoc", "-f", "XML", libName,
                    file.getAbsolutePath());

            runLibdoc(libName, cmdLine);
        }
    }


    private void runLibdoc(final String libName, final List<String> cmdLine)
            throws RobotEnvironmentException {
        try {
            final List<String> lines = newArrayList();
            final ILineHandler handler = new ILineHandler(){

                @Override
                public void processLine(final String line) {
                    lines.add(line);
                }
            };
            final int returnCode = runExternalProcess(cmdLine, handler);
            if (returnCode != 0) {
                throw new RobotEnvironmentException(
                        "Unable to generate library specification file for library "
                                + libName + "\nDetailed information:\n"
                                + Joiner.on('\n').join(lines));
            }
        } catch (final IOException e) {
            throw new RobotEnvironmentException(
                    "Unable to generate library specification file for library "
                            + libName, e);
        }
    }


    public List<String> getStandardLibrariesNames() {
        if (hasRobotInstalled()) {
            try {
                final File scriptFile = copyResourceFile("StdLibrariesReader.py");

                final String cmd = getPythonExecutablePath((PythonInstallationDirectory) location);
                final List<String> cmdLine = Arrays.asList(cmd,
                        scriptFile.getAbsolutePath());
                final List<String> stdLibs = new ArrayList<>();
                final ILineHandler linesHandler = new ILineHandler(){

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
            final List<String> cmdLine = Arrays.asList(cmd, "-c",
                    "import robot.libraries." + libraryName
                            + ";print(robot.libraries." + libraryName
                            + ".__file__)");
            final StringBuilder path = new StringBuilder();
            final ILineHandler linesHandler = new ILineHandler(){

                @Override
                public void processLine(final String line) {
                    path.append(line);
                }
            };

            try {
                runExternalProcess(cmdLine, linesHandler);
                final String pycPath = path.toString().trim();
                if (pycPath.endsWith(".py")) {
                    return new File(pycPath);
                } else if (pycPath.endsWith(".pyc")) {
                    return new File(pycPath.substring(0, pycPath.length() - 1));
                } else if (pycPath.endsWith("$py.class")) {
                    return new File(pycPath.substring(0, pycPath.length() - 9)
                            + ".py");
                }
                return null;
            } catch (final IOException e) {
                return null;
            }
        }
        return null;
    }


    public Map<?, ?> getVariablesFromFile(final String path,
            final List<String> args) {
        Map<?, ?> variables = new LinkedHashMap<>();

        if (hasRobotInstalled()) {
            final String cmd = getPythonExecutablePath((PythonInstallationDirectory) location);
            final StringBuilder argsBuilder = new StringBuilder();
            if (args != null && !args.isEmpty()) {
                argsBuilder.append("[");
                for (int i = 0; i < args.size(); i++) {
                    argsBuilder.append("'" + args.get(i) + "'");
                    if (i < args.size() - 1) {
                        argsBuilder.append(",");
                    }
                }
                argsBuilder.append("]");
            } else {
                argsBuilder.append("None");
            }

            final String normalizedPath = path.replace('\\', '/');
            final List<String> cmdLine = Arrays
                    .asList(cmd,
                            "-c",
                            "import robot.variables as rv;vars=rv.Variables();vars.set_from_file('"
                                    + normalizedPath
                                    + "',"
                                    + argsBuilder.toString()
                                    + ");exec('try:\\n\\tprint(str(vars.data))\\n"
                                    + "except AttributeError:\\n\\tprint(str(vars.store.data))')");

            final StringBuilder result = new StringBuilder();
            final ILineHandler linesHandler = new ILineHandler(){

                @Override
                public void processLine(final String line) {
                    result.append(line);
                }
            };

            String resultVars = "";
            try {
                runExternalProcess(cmdLine, linesHandler);
                resultVars = result.toString().trim().replaceAll("'", "\"");
                if (mapper == null) {
                    mapper = new ObjectMapper();
                }
                variables = mapper.readValue(resultVars, Map.class);
            } catch (final IOException e) {
                e.printStackTrace();
                System.out.println("Command line output " + resultVars);
            }
        }
        return variables;
    }


    public RunCommandLine createCommandLineCall(final SuiteExecutor executor,
            final List<String> classpath, final List<String> pythonpath,
            final List<String> variableFilesPath, final File projectLocation,
            final List<String> suites, final String userArguments,
            final List<String> includedTags, final List<String> excludedTags,
            final boolean isDebugging) throws IOException {

        final String debugInfo = isDebugging ? "True" : "False";
        final int port = findFreePort();

        final List<String> cmdLine = new ArrayList<String>(getRunCommandLine(
                executor, classpath, pythonpath, variableFilesPath));

        addTags(cmdLine, includedTags, excludedTags);

        cmdLine.add("--listener");
        cmdLine.add(copyResourceFile("TestRunnerAgent.py").toPath() + ":"
                + port + ":" + debugInfo);

        for (final String suite : suites) {
            cmdLine.add("--suite");
            cmdLine.add(suite);
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
            argumentClasspath(cmdLine, executor, classpath);
            cmdLine.add("-m");
            cmdLine.add("robot.run");
        } else {
            cmdLine.add(executor.executableName());
            argumentClasspath(cmdLine, executor, classpath);
            cmdLine.add("\""
                    + getRunModulePath((PythonInstallationDirectory) location)
                    + "\"");
        }
        addPythonpath(cmdLine, pythonpath);
        addVariableFilesPath(cmdLine, variableFilesPath);
        return cmdLine;
    }


    private void argumentClasspath(final List<String> cmdLine,
            final SuiteExecutor executor, final List<String> classpath) {
        if (executor == SuiteExecutor.Jython) {
            final String cpSeparator = isWindows() ? ";" : ":";
            cmdLine.add("-J-cp");
            cmdLine.add("\"" + Joiner.on(cpSeparator).join(classpath) + "\"");
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
        for (String tag : includedTags) {
            cmdLine.add("-i");
            cmdLine.add(tag);
        }
        for (String tag : excludedTags) {
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
    public static class RobotEnvironmentException extends Exception {

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
