package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import javax.xml.stream.Location;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.RobotProjectConfigWithLines;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ConfigFileProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.reporting.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.editor.JarStructureBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.JarStructureBuilder.JarClass;

class RobotProjectConfigFileValidator {

    private final IFile configFile;

    RobotProjectConfigFileValidator(final IFile configFile) {
        this.configFile = configFile;
    }

    void validate(final IProgressMonitor monitor) throws CoreException {
        final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();
        RobotProjectConfigWithLines config = null;
        try {
            config = new RobotProjectConfigReader().readConfigurationWithLines(configFile);
        } catch (final CannotReadProjectConfigurationException e) {
            // this problem is handled by RobotLibraries builder
            return;
        }

        final RobotProjectConfig model = config.getConfigurationModel();
        final Map<Object, Location> linesMapping = config.getLinesMapping();
        for (final RemoteLocation location : model.getRemoteLocations()) {
            if (monitor.isCanceled()) {
                return;
            }
            validateRemoteLocation(location, configFile, linesMapping, reporter);
        }

        for (final ReferencedLibrary library : model.getLibraries()) {
            if (monitor.isCanceled()) {
                return;
            }
            validateReferencedLibrary(library, configFile, linesMapping, reporter);
        }
    }

    private void validateRemoteLocation(final RemoteLocation location, final IFile configFile,
            final Map<Object, Location> linesMapping, final ProblemsReportingStrategy reporter) throws CoreException {
        String address = location.getPath();
        final String httpPrefix = "http://";
        if (address.startsWith(httpPrefix)) {
            address = address.substring(httpPrefix.length());
        }
        try (Socket s = new Socket(address, location.getPort())) {
            // that's fine
        } catch (final IOException ex) {
            final RobotProblem unreachableHostProblem = RobotProblem.causedBy(ConfigFileProblem.UNREACHABLE_HOST)
                    .formatMessageWith(address, location.getPort());
            reporter.handleProblem(unreachableHostProblem, configFile, linesMapping.get(location).getLineNumber());
        }
    }

    private void validateReferencedLibrary(final ReferencedLibrary library, final IFile configFile,
            final Map<Object, Location> linesMapping, final ProblemsReportingStrategy reporter) {
        final LibraryType libType = library.provideType();
        final IPath libraryPath = Path.fromPortableString(library.getPath());
        final int lineNumber = linesMapping.get(library).getLineNumber();
        switch (libType) {
            case JAVA:
                reporter.handleProblem(findJavaLibaryProblem(libraryPath, library.getName()),
                    configFile, lineNumber);
                break;
            case VIRTUAL:
                reporter.handleProblem(findVirtualLibaryProblem(libraryPath), configFile, lineNumber);
                break;
            default:
                break;
        }

    }

    private RobotProblem findJavaLibaryProblem(final IPath libraryPath, final String libName) {
        final File filePath = libraryPath.toFile();
        if (!"jar".equals(libraryPath.getFileExtension())) {
            return RobotProblem.causedBy(ConfigFileProblem.JAVA_LIB_NOT_A_JAR_FILE)
                    .formatMessageWith(libraryPath);
        }
        if (!filePath.exists()) {
            return RobotProblem.causedBy(ConfigFileProblem.MISSING_JAR_FILE).formatMessageWith(
                    libraryPath);
        } else {
            boolean containsClass = false;
            for (final JarClass jarClass : new JarStructureBuilder().provideEntriesFromJarFile(filePath.toString())) {
                if (jarClass.getQualifiedName().equals(libName)) {
                    containsClass = true;
                    break;
                }
            }
            if (!containsClass) {
                return RobotProblem.causedBy(ConfigFileProblem.JAVA_LIB_MISSING_CLASS)
                        .formatMessageWith(libraryPath, libName);
            }
        }
        return null;
    }

    private RobotProblem findVirtualLibaryProblem(final IPath libraryPath) {
        if (libraryPath.isAbsolute()) {
            return RobotProblem.causedBy(ConfigFileProblem.ABSOLUTE_PATH).formatMessageWith(
                    libraryPath);
        }
        final IResource libspec = ResourcesPlugin.getWorkspace().getRoot().findMember(libraryPath);
        if (libspec == null || !libspec.exists()) {
            return RobotProblem.causedBy(ConfigFileProblem.MISSING_LIBSPEC_FILE)
                    .formatMessageWith(libraryPath);
        }
        return null;
    }
}
