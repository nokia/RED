package org.robotframework.ide.eclipse.main.plugin.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.project.library.Libraries.createRefLibs;
import static org.robotframework.ide.eclipse.main.plugin.project.library.Libraries.createRemoteLib;
import static org.robotframework.ide.eclipse.main.plugin.project.library.Libraries.createStdLibs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.Multimap;

public class RobotSuiteFileTest {

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(RobotSuiteFileTest.class);

    private RobotModel robotModel;

    @Before
    public void beforeTest() throws Exception {
        robotModel = new RobotModel();
    }

    @After
    public void afterTest() {
        robotModel = null;
    }
    
    @Test
    public void librarySpecsAreReturned_whenSuiteImportsLibrariesByName() throws Exception {
        final IFile file = projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library",
                "Library  Collections",
                "Library  myLib");
        final RobotSuiteFile fileModel = robotModel.createSuiteFile(file);
    
        final RobotProject robotProject = robotModel.createRobotProject(file.getProject());
        robotProject.setStandardLibraries(createStdLibs("Collections", "OperatingSystem"));
        robotProject.setReferencedLibraries(createRefLibs("myLib", "myLib2"));
    
        final Multimap<LibrarySpecification, Optional<String>> imported = fileModel.getImportedLibraries();
    
        assertThat(imported.keySet()).hasSize(2);
        assertThat(imported.keySet().stream().map(LibrarySpecification::getName)).containsOnly("Collections", "myLib");
    }

    @Test
    public void librarySpecsAreReturned_whenSuiteImportsMultipleDifferentRemoteLibraries() throws Exception {
        final IFile file = projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  Remote  http://1.2.3.4/mylib",
                "Library  Remote  http://5.6.7.8/mylib2");
        final RobotSuiteFile fileModel = robotModel.createSuiteFile(file);

        final RobotProject robotProject = robotModel.createRobotProject(file.getProject());
        robotProject.setStandardLibraries(merge(
                createRemoteLib("http://1.2.3.4/mylib"),
                createRemoteLib("http://5.6.7.8/mylib2")));

        final Multimap<LibrarySpecification, Optional<String>> imported = fileModel.getImportedLibraries();

        assertThat(imported.keySet()).hasSize(2);
        assertThat(imported.keySet().stream().map(LibrarySpecification::getName)).containsOnly("Remote");
        assertThat(imported.keySet().stream().map(LibrarySpecification::getRemoteLocation)).containsOnly(
                RemoteLocation.create("http://1.2.3.4/mylib"), RemoteLocation.create("http://5.6.7.8/mylib2"));
    }

    @SafeVarargs
    private static Map<String, LibrarySpecification> merge(final Map<String, LibrarySpecification>... libs) {
        final Map<String, LibrarySpecification> merged = new HashMap<>();
        for (final Map<String, LibrarySpecification> lib : libs) {
            merged.putAll(lib);
        }
        return merged;
    }
}
