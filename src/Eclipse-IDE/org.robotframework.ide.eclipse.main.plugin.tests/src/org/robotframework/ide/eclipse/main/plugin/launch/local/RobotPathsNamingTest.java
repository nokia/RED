/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.core.resources.IResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.junit.jupiter.RedTempDirectory;
import org.robotframework.red.junit.jupiter.StatefulProject;

import com.google.common.collect.ImmutableMap;

@ExtendWith({ ProjectExtension.class, RedTempDirectory.class })
public class RobotPathsNamingTest {

    @TempDir
    public static File tempFolder;

    @Project
    public static StatefulProject project;

    private static final Function<IResource, List<String>> LAST_SEGMENT = r -> newArrayList(
            r.getLocation().removeFileExtension().lastSegment());

    private static final Function<IResource, List<String>> ALL_SEGMENTS = r -> newArrayList(
            r.getFullPath().removeFileExtension().segments());

    @BeforeAll
    public static void beforeSuite() throws Exception {
        final File nonWorkspaceFile = new File(tempFolder, "linked_suite.robot");
        nonWorkspaceFile.createNewFile();

        project.createFileLink("ProjectLink.robot", nonWorkspaceFile.toURI());
    }

    @Test
    public void testCreatingTopLevelSuiteName() throws Exception {
        SoftAssertions.assertSoftly(softly -> {
            final String projectName = project.getName();

            softly.assertThat(RobotPathsNaming.createTopLevelSuiteName(newArrayList())).isEmpty();

            softly.assertThat(RobotPathsNaming.createTopLevelSuiteName(newArrayList(project.getProject()))).isEmpty();

            softly.assertThat(RobotPathsNaming.createTopLevelSuiteName(
                    newArrayList(project.getProject(), project.getDir("Some Folder"))))
                    .isEqualTo(projectName + " & Some Folder");

            softly.assertThat(RobotPathsNaming.createTopLevelSuiteName(newArrayList(project.getProject(),
                    project.getDir("Some Folder"), project.getFile("Some Suite.robot"))))
                    .isEqualTo(projectName + " & Some Folder & Some Suite");

            softly.assertThat(RobotPathsNaming.createTopLevelSuiteName(
                    newArrayList(project.getProject(), project.getDir("underscored_folder"),
                            project.getFile("001__prefixed_suite.robot"))))
                    .isEqualTo(projectName + " & Underscored Folder & Prefixed Suite");

            softly.assertThat(RobotPathsNaming.createTopLevelSuiteName(
                    newArrayList(project.getProject(), project.getFile("ProjectLink.robot"))))
                    .isEqualTo(projectName + " & Linked Suite");
        });
    }

    @Test
    public void testCreatingSuiteNames() throws Exception {
        SoftAssertions.assertSoftly(softly -> {
            final String projectName = project.getName();

            softly.assertThat(RobotPathsNaming.createSuiteNames(ImmutableMap.of(), "", LAST_SEGMENT, 0)).isEmpty();

            softly.assertThat(RobotPathsNaming
                    .createSuiteNames(ImmutableMap.of(project.getProject(), newArrayList()), "", LAST_SEGMENT,
                            0))
                    .containsExactly(projectName);

            softly.assertThat(RobotPathsNaming.createSuiteNames(
                    ImmutableMap.of(project.getFile("suite.robot"), newArrayList()), "", LAST_SEGMENT, 0))
                    .containsExactly("Suite");

            softly.assertThat(RobotPathsNaming.createSuiteNames(
                    ImmutableMap.of(project.getFile("suite.robot"), newArrayList()), "Top", LAST_SEGMENT, 1))
                    .containsExactly("Top");

            softly.assertThat(RobotPathsNaming.createSuiteNames(
                    ImmutableMap.of(project.getFile("suite.robot"), newArrayList(),
                            project.getFile("ProjectLink.robot"), newArrayList()),
                    "Top", LAST_SEGMENT, 0)).contains("Top.Suite", "Top.Linked Suite");

            softly.assertThat(RobotPathsNaming.createSuiteNames(
                    ImmutableMap.of(project.getFile("ProjectLink.robot"), newArrayList()), "", LAST_SEGMENT, 0))
                    .containsExactly("Linked Suite");

            softly.assertThat(RobotPathsNaming.createSuiteNames(
                    ImmutableMap.of(project.getFile("suite.robot"), newArrayList("tc1", "tc2")), "",
                    LAST_SEGMENT, 0)).containsExactly("Suite");

            softly.assertThat(RobotPathsNaming.createSuiteNames(
                    ImmutableMap.of(project.getFile("some/path/to/suite.robot"), newArrayList()), "",
                    ALL_SEGMENTS, 0)).containsExactly(projectName + ".Some.Path.To.Suite");

            softly.assertThat(
                    RobotPathsNaming.createSuiteNames(
                            ImmutableMap.of(project.getFile("001__same_suite.robot"), newArrayList(),
                                    project.getFile("002__same_suite.robot"), newArrayList(),
                                    project.getFile("003__same_suite.robot"), newArrayList()),
                            "", ALL_SEGMENTS, 0))
                    .containsExactly(projectName + ".Same Suite");

            softly.assertThat(RobotPathsNaming.createSuiteNames(
                    ImmutableMap.of(project.getFile("some/path/to/suite.robot"), newArrayList()), "Top",
                    ALL_SEGMENTS, 1)).containsExactly("Top.Some.Path.To.Suite");

            softly.assertThat(
                    RobotPathsNaming.createSuiteNames(
                            ImmutableMap.of(project.getFile("very/very/long/path/to/suite.robot"), newArrayList(),
                                    project.getFile("another/path/to/test.robot"), newArrayList(),
                                    project.getDir("different/folder"), newArrayList()),
                            "TopName", ALL_SEGMENTS, 0))
                    .containsExactly("TopName." + projectName + ".Very.Very.Long.Path.To.Suite",
                            "TopName." + projectName + ".Another.Path.To.Test",
                            "TopName." + projectName + ".Different.Folder");
        });
    }

    @Test
    public void testCreatingTestNames() throws Exception {
        SoftAssertions.assertSoftly(softly -> {
            final String projectName = project.getName();

            softly.assertThat(RobotPathsNaming.createTestNames(ImmutableMap.of(), "", LAST_SEGMENT, 1)).isEmpty();

            softly.assertThat(RobotPathsNaming.createTestNames(
                    ImmutableMap.of(project.getFile("suite.robot"), newArrayList("tc1", "tc2")), "",
                    LAST_SEGMENT, 0)).containsExactly("Suite.tc1", "Suite.tc2");

            softly.assertThat(RobotPathsNaming.createTestNames(
                    ImmutableMap.of(project.getFile("suite.robot"), newArrayList("tc1", "tc2")), "Top Name",
                    LAST_SEGMENT, 1)).containsExactly("Top Name.tc1", "Top Name.tc2");

            softly.assertThat(RobotPathsNaming.createTestNames(
                    ImmutableMap.of(project.getFile("001__same_suite.robot"), newArrayList("tc1", "tc2"),
                            project.getFile("002__same_suite.robot"), newArrayList("tc1", "tc2"),
                            project.getFile("003__same_suite.robot"), newArrayList("tc1", "tc2")),
                    "", ALL_SEGMENTS, 0))
                    .containsExactly(projectName + ".Same Suite.tc1", projectName + ".Same Suite.tc2");

            softly.assertThat(RobotPathsNaming.createTestNames(
                    ImmutableMap.of(project.getFile("ProjectLink.robot"), newArrayList("tc1", "tc2")), "",
                    LAST_SEGMENT, 0)).containsExactly("Linked Suite.tc1", "Linked Suite.tc2");

            softly.assertThat(RobotPathsNaming.createTestNames(
                    ImmutableMap.of(project.getFile("test/suite.robot"), newArrayList("tc1", "tc2", "tc3")),
                    "TopName", ALL_SEGMENTS, 1))
                    .containsExactly("TopName.Test.Suite.tc1", "TopName.Test.Suite.tc2", "TopName.Test.Suite.tc3");

            softly.assertThat(RobotPathsNaming.createTestNames(
                    ImmutableMap.of(project.getFile("path/first.robot"), newArrayList("tc1"),
                            project.getFile("path/second.robot"), newArrayList("tc1", "tc2"),
                            project.getFile("path/third.robot"), newArrayList("tc1", "tc2", "tc3")),
                    "TopName", ALL_SEGMENTS, 0))
                    .containsExactly("TopName." + projectName + ".Path.First.tc1",
                            "TopName." + projectName + ".Path.Second.tc1",
                            "TopName." + projectName + ".Path.Second.tc2", "TopName." + projectName + ".Path.Third.tc1",
                            "TopName." + projectName + ".Path.Third.tc2", "TopName." + projectName + ".Path.Third.tc3");
        });
    }

    @Test
    public void testCreatingSuiteName() throws Exception {
        SoftAssertions.assertSoftly(softly -> {
            final String projectName = project.getName();

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList(), 0)).isEmpty();

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList(""), 0)).isEmpty();

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("a"), 0)).isEqualTo("A");

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("a", "b", "c"), 0))
                    .isEqualTo("A.B.C");

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("abc"), 0)).isEqualTo("Abc");

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("a_b_c"), 0)).isEqualTo("A B C");

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("some path", "to suite"), 0))
                    .isEqualTo("Some Path.To Suite");

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("some_path", "to_suite"), 0))
                    .isEqualTo("Some Path.To Suite");

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("a", "001__b", "c"), 0))
                    .isEqualTo("A.B.C");

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("001__a__b_c", "001__d__e_f"), 0))
                    .isEqualTo("A B C.D E F");

            softly.assertThat(RobotPathsNaming.createSuiteName(projectName, newArrayList("a", "b", "c"), 0))
                    .isEqualTo(projectName + ".A.B.C");

            softly.assertThat(RobotPathsNaming.createSuiteName("Path1 & Path2", newArrayList("a", "b", "c"), 0))
                    .isEqualTo("Path1 & Path2.A.B.C");
        });
    }

    @Test
    public void testCreatingTestName() throws Exception {
        SoftAssertions.assertSoftly(softly -> {
            final String projectName = project.getName();

            softly.assertThat(RobotPathsNaming.createTestName("", newArrayList("path", "to", "suite"), "case", 0))
                    .isEqualTo("Path.To.Suite.case");

            softly.assertThat(RobotPathsNaming.createTestName(projectName, newArrayList("path", "to", "suite"),
                    "project test case", 0)).isEqualTo(projectName + ".Path.To.Suite.project test case");

            softly.assertThat(RobotPathsNaming.createTestName(projectName, newArrayList("20__Some_path", "to_suite"),
                    "Test case", 0)).isEqualTo(projectName + ".Some Path.To Suite.Test case");

            softly.assertThat(RobotPathsNaming.createTestName("Path1 & Path2", newArrayList("path", "to", "suite"),
                    "100__test_case", 0)).isEqualTo("Path1 & Path2.Path.To.Suite.100__test_case");
        });
    }
}
