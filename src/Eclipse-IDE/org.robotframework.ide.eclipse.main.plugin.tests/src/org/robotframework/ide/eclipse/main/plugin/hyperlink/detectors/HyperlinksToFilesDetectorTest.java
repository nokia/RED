/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.FileHyperlink;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

import com.google.common.collect.ImmutableMap;

@ExtendWith(ProjectExtension.class)
public class HyperlinksToFilesDetectorTest {

    @Project(dirs = { "directory" }, files = { "file.robot", "lib.py", "unhandled.txt", "directory/file.robot" })
    static IProject project;

    @Test
    public void noHyperlinksAreProvided_whenLibraryIsImportedUsingName() {
        final RobotSuiteFile robotFile = new RobotModel().createSuiteFile(getFile(project, "file.robot"));

        final HyperlinksToFilesDetector detector = createDetector();
        assertThat(detector.detectHyperlinks(robotFile, new Region(50, 10), "lib_name", true)).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenUsingAbsolutePathWhichIsOutsideWorkspace() {
        final RobotSuiteFile robotFile = new RobotModel().createSuiteFile(getFile(project, "file.robot"));

        final HyperlinksToFilesDetector detector = createDetector();
        assertThat(detector.detectHyperlinks(robotFile, new Region(50, 10), "/abs_path/to_file.robot", false))
                .isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenPathPointsToDirectory() {
        final RobotSuiteFile robotFile = new RobotModel().createSuiteFile(getFile(project, "file.robot"));

        final String relPath = "directory/";
        final String absPath = project.getLocation().append(relPath).toString();

        final HyperlinksToFilesDetector detector = createDetector();
        assertThat(detector.detectHyperlinks(robotFile, new Region(50, 10), relPath, true)).isEmpty();
        assertThat(detector.detectHyperlinks(robotFile, new Region(50, 10), absPath, true)).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenPathPointsNonExistingFile() {
        final RobotSuiteFile robotFile = new RobotModel().createSuiteFile(getFile(project, "file.robot"));

        final String relPath = "directory/non_existing.robot";
        final String absPath = project.getLocation().append(relPath).toString();

        final HyperlinksToFilesDetector detector = createDetector();
        assertThat(detector.detectHyperlinks(robotFile, new Region(50, 10), relPath, true)).isEmpty();
        assertThat(detector.detectHyperlinks(robotFile, new Region(50, 10), absPath, true)).isEmpty();
    }

    @Test
    public void fileHyperlinkIsProvided_whenPathPointsToExistingFile() {
        final RobotSuiteFile robotFile = new RobotModel().createSuiteFile(getFile(project, "file.robot"));

        final String relPath = "file.robot";
        final String absPath = project.getLocation().append(relPath).toString();

        final HyperlinksToFilesDetector detector = createDetector();

        final List<IHyperlink> hyperlinks1 = detector.detectHyperlinks(robotFile, new Region(50, 10), relPath, false);
        assertThat(hyperlinks1).hasSize(1).allMatch(FileHyperlink.class::isInstance);
        final List<IHyperlink> hyperlinks2 = detector.detectHyperlinks(robotFile, new Region(50, 10), absPath, false);
        assertThat(hyperlinks2).hasSize(1).allMatch(FileHyperlink.class::isInstance);
    }

    @Test
    public void fileHyperlinkIsProvided_whenPathIsParameterized() {
        final RobotSuiteFile robotFile = new RobotModel().createSuiteFile(getFile(project, "file.robot"));
        final RobotProject robotProject = robotFile.getRobotProject();
        robotProject.setRobotProjectConfig(new RobotProjectConfig());
        robotProject
                .getRobotProjectHolder()
                .setVariableMappings(ImmutableMap.of("${varname}", "file", "${varpath}", "file.robot"));

        final HyperlinksToFilesDetector detector = createDetector();

        final List<IHyperlink> hyperlinks1 = detector.detectHyperlinks(robotFile, new Region(50, 10),
                "${varName}.robot", false);
        assertThat(hyperlinks1).hasSize(1).allMatch(FileHyperlink.class::isInstance);
        final List<IHyperlink> hyperlinks2 = detector.detectHyperlinks(robotFile, new Region(50, 10),
                "${varPath}", false);
        assertThat(hyperlinks2).hasSize(1).allMatch(FileHyperlink.class::isInstance);
    }

    @Test
    public void fileHyperlinkIsProvided_whenPathIsParameterizedInLibraryImport() {
        final RobotSuiteFile robotFile = new RobotModel().createSuiteFile(getFile(project, "file.robot"));
        final RobotProject robotProject = robotFile.getRobotProject();
        robotProject.setRobotProjectConfig(new RobotProjectConfig());
        robotProject.getRobotProjectHolder()
                .setVariableMappings(ImmutableMap.of("${libname}", "lib", "${libpath}", "lib.py"));

        final HyperlinksToFilesDetector detector = createDetector();

        final List<IHyperlink> hyperlinks1 = detector.detectHyperlinks(robotFile, new Region(50, 10),
                "${libName}.py", true);
        assertThat(hyperlinks1).hasSize(1).allMatch(FileHyperlink.class::isInstance);
        final List<IHyperlink> hyperlinks2 = detector.detectHyperlinks(robotFile, new Region(50, 10), "${libPath}",
                true);
        assertThat(hyperlinks2).hasSize(1).allMatch(FileHyperlink.class::isInstance);
    }

    private static HyperlinksToFilesDetector createDetector() {
        return new HyperlinksToFilesDetector() {

            @SuppressWarnings("unchecked")
            @Override
            protected Consumer<IFile> performAfterOpening() {
                return mock(Consumer.class);
            }
        };
    }
}
