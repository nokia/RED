package org.robotframework.ide.eclipse.main.plugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.OS;
import org.apache.commons.exec.PumpStreamHandler;

import com.google.common.base.Objects;

public class RobotRuntimeEnvironment {

    /**
     * Locates directory in which python pointed in environment path is located.
     * Uses where command in Windwows and which command under Unix.
     * 
     * @return Directory where python is installed or null if there is no
     *         python.
     */
    public static PythonInstallationDirectory whereIsDefaultPython() {
        final String cmd = OS.isFamilyWindows() ? "where python" : "which python";
        final StringBuilder whereOutput = new StringBuilder();

        final PumpStreamHandler handler = new PumpStreamHandler(new LogOutputStream() {

            @Override
            protected void processLine(final String line, final int level) {
                whereOutput.append(line).append(System.lineSeparator());
            }
        });

        final CommandLine cmdLine = CommandLine.parse(cmd);
        final DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(handler);
        try {
            final int returnCode = executor.execute(cmdLine);
            if (returnCode == 0) {
                final URI dirUri = new File(whereOutput.toString()).getParentFile().toURI();
                return new PythonInstallationDirectory(dirUri);
            }
        } catch (final IOException e) {
            return null;
        }
        // FIXME : this probably does not work on linux since usually there are
        // symlinks defined, so they should be followed
        return null;
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
        if (OS.isFamilyWindows()) {
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
        final String pythonExe = OS.isFamilyWindows() ? "python.exe" : "python";
        final String cmd = findFile(pythonLocation, pythonExe).getAbsolutePath() + " -m robot.run --version";
        final StringBuilder whereOutput = new StringBuilder();

        final PumpStreamHandler handler = new PumpStreamHandler(new LogOutputStream() {

            @Override
            protected void processLine(final String line, final int level) {
                whereOutput.append(line).append(System.lineSeparator());
            }
        });

        final CommandLine cmdLine = CommandLine.parse(cmd);
        final DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(handler);
        try {
            executor.execute(cmdLine);
        } catch (final IOException e) {
            // the python -m robot.run --version call returns non zero code even if everything is fine
        }

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

    public void installRobotUsingPip(final OutputStream stream, final boolean useStableVersion)
            throws RobotEnvironmentException {
        if (isValidPythonInstallation()) {
            final String pythonExec = OS.isFamilyWindows() ? "python.exe" : "python";
            final CommandLine cmdLine = new CommandLine(findFile(location, pythonExec));
            cmdLine.addArgument("-m");
            cmdLine.addArgument("pip");
            cmdLine.addArgument("install");
            cmdLine.addArgument("--upgrade");
            if (!useStableVersion) {
                cmdLine.addArgument("--pre");
            }
            cmdLine.addArgument("robotframework");
            final DefaultExecutor executor = new DefaultExecutor();
            executor.setStreamHandler(new PumpStreamHandler(stream));
            try {
                executor.execute(cmdLine);
                version = getRobotFrameworkVersion((PythonInstallationDirectory) location);
            } catch (final IOException e) {
                throw new RobotEnvironmentException(
                        "There was a problem installing Robot Framework. Check if this Python installation has pip module provided.");
            }
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
