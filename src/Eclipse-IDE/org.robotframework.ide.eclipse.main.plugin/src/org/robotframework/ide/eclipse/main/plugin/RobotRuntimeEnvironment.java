package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Objects;

public class RobotRuntimeEnvironment {

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }

    private static int runExternalProcess(final List<String> command, final ProcessLineHandler linesHandler)
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
     * Locates directory in which python pointed in environment path is located.
     * Uses where command in Windwows and which command under Unix.
     * 
     * @return Directory where python is installed or null if there is no
     *         python.
     */
    public static PythonInstallationDirectory whereIsDefaultPython() {
        final StringBuilder whereOutput = new StringBuilder();
        final ProcessLineHandler linesProcessor = new ProcessLineHandler() {
            @Override
            public void processLine(final String line) {
                whereOutput.append(line);
            }
        };

        try {
            final String cmd = isWindows() ? "where" : "which";
            final int returnCode = runExternalProcess(Arrays.asList(cmd, "python"), linesProcessor);
            if (returnCode == 0) {
                final URI dirUri = new File(whereOutput.toString()).getParentFile().toURI();
                return new PythonInstallationDirectory(dirUri);
            } else {
                return null;
            }
        } catch (final IOException e) {
            return null;
        }
    }

    /**
     * Checks if given location is a directory which contains python
     * installation. The {@link IllegalArgumentException} exception is thrown if
     * given location does not contain python executable. Otherwise
     * {@link PythonInstallationDirectory} instance (copy of location, but with other
     * type) is returned.
     * 
     * @param location
     *            Location to check
     * @return the same location given as {@link File} subtype
     * @throws IllegalArgumentException
     *             thrown when given location is not a directory or does not
     *             contain python executables.
     */
    private static PythonInstallationDirectory checkPythonInstallationDir(final File location)
            throws IllegalArgumentException {
        if (isWindows()) {
            if (!location.isDirectory()) {
                throw new IllegalArgumentException("The location " + location.getAbsolutePath()
                        + " is not a directory.");
            }
            final List<String> names = Arrays.asList(location.list());
            if (!names.contains("python.exe")) {
                throw new IllegalArgumentException("The location: " + location.getAbsolutePath()
                        + " does not seem to be a valid python installation directory");
            }
            return new PythonInstallationDirectory(location.toURI());
        }
        // FIXME : check in linux
        throw new IllegalArgumentException("This is not yet implemented on unix!");
    }


    /**
     * Gets robot framework version as returned by following call:
     *      python -m robot.run --version
     *      
     * @param pythonLocation
     * @return Robot version as returned by robot
     */
    private static String getRobotFrameworkVersion(final PythonInstallationDirectory pythonLocation) {
        final StringBuilder versionOutput = new StringBuilder();
        final ProcessLineHandler linesHandler = new ProcessLineHandler() {
            @Override
            public void processLine(final String line) {
                versionOutput.append(line);
            }
        };

        try {
            final String pythonExe = isWindows() ? "python.exe" : "python";
            final String cmd = findFile(pythonLocation, pythonExe).getAbsolutePath();
            runExternalProcess(Arrays.asList(cmd, "-m", "robot.run", "--version"), linesHandler);
            final String output = versionOutput.toString();
            return output.startsWith("Robot Framework") ? output.trim() : null;
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static File findFile(final File pythonLocation, final String name) {
        for (final File file : pythonLocation.listFiles()) {
            if (name.equals(file.getName())) {
                return file;
            }
        }
        return null;
    }

    private final File location;

    private String version;

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

    public File getFile() {
        return location;
    }

    public void installRobotUsingPip(final ProcessLineHandler linesHandler, final boolean useStableVersion)
            throws RobotEnvironmentException {
        if (isValidPythonInstallation()) {
            final String pythonExec = isWindows() ? "python.exe" : "python";
            final String cmd = findFile(location, pythonExec).getAbsolutePath();
            final List<String> cmdLine = newArrayList();
            cmdLine.addAll(Arrays.asList(cmd, "-m", "pip", "install", "-upgrade"));
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

    public List<String> getStandardLibrariesNames() {
        if (hasRobotInstalled()) {
            final String pythonExec = isWindows() ? "python.exe" : "python";
            final String cmd = findFile(location, pythonExec).getAbsolutePath();
            final String pythonCode = "import robot.running.namespace; print('\\n'.join(robot.running.namespace.STDLIB_NAMES))";
            final List<String> cmdLine = newArrayList(cmd, "-c", "\"" + pythonCode + "\"");
            final List<String> stdLibs = newArrayList();
            final ProcessLineHandler linesHandler = new ProcessLineHandler() {
                @Override
                public void processLine(final String line) {
                    stdLibs.add(line.trim());
                }
            };

            try {
                runExternalProcess(cmdLine, linesHandler);
                return stdLibs;
            } catch (final IOException e) {
                return stdLibs;
            }
        } else {
            return newArrayList();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(location, version);
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
        return Objects.equal(location, other.location) && Objects.equal(version, other.version);
    }

    public interface ProcessLineHandler {
        public void processLine(final String line);
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
        
        // we dont' want anyone to create those objects; they should only be created
        // when given uri is valid python location
        private PythonInstallationDirectory(final URI uri) {
            super(uri);
        }
    }
}
