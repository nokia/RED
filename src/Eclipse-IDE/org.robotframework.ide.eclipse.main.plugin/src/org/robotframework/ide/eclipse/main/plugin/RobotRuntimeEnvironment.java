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

    private static int runExternalProcess(final List<String> command, final ProcessLineHandler linesHandler) {
        try {
            final Process process = new ProcessBuilder(command).start();

            final InputStream inputStream = process.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                linesHandler.processLine(line, 0);
            }
            return process.waitFor();
        } catch (final IOException | InterruptedException e) {
            // FIXME : handle that
            e.printStackTrace();
            return -100;
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
        final String cmd = isWindows() ? "where" : "which";

        final StringBuilder whereOutput = new StringBuilder();
        final int returnCode = runExternalProcess(Arrays.asList(cmd, "python"), new ProcessLineHandler() {
            @Override
            public void processLine(final String line, final int level) {
                whereOutput.append(line);
            }
        });
        if (returnCode == 0) {
            final URI dirUri = new File(whereOutput.toString()).getParentFile().toURI();
            return new PythonInstallationDirectory(dirUri);
        } else {
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
        final String pythonExe = isWindows() ? "python.exe" : "python";
        final String cmd = findFile(pythonLocation, pythonExe).getAbsolutePath();

        final StringBuilder whereOutput = new StringBuilder();
        runExternalProcess(Arrays.asList(cmd, "-m", "robot.run", "--version"), new ProcessLineHandler() {
            @Override
            public void processLine(final String line, final int level) {
                whereOutput.append(line).append(System.lineSeparator());
            }
        });
        final String output = whereOutput.toString();
        return isProperVersion(output) ? output.trim() : null;
    }

    private static boolean isProperVersion(final String output) {
        return output.startsWith("Robot Framework");
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
        try {
            final PythonInstallationDirectory location = checkPythonInstallationDir(new File(pathToPython));
            return new RobotRuntimeEnvironment(location, getRobotFrameworkVersion(location));
        } catch (final IllegalArgumentException e) {
            return new RobotRuntimeEnvironment(new File(pathToPython), null);
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
            final List<String> cmdLine = newArrayList();
            cmdLine.addAll(Arrays.asList(pythonExec, "-m", "pip", "install", "-upagrade"));
            if (!useStableVersion) {
                cmdLine.add("--pre");
            }
            cmdLine.add("robotframework");
            runExternalProcess(cmdLine, linesHandler);
            version = getRobotFrameworkVersion((PythonInstallationDirectory) location);
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
        public void processLine(final String line, final int level);
    }

    public static class RobotEnvironmentException extends Exception {

        public RobotEnvironmentException(final String message) {
            super(message);
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
