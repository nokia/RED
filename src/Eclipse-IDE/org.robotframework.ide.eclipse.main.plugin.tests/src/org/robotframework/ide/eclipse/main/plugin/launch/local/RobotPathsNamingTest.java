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
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ResourceCreator;

import com.google.common.collect.ImmutableMap;

public class RobotPathsNamingTest {

    private static final String PROJECT_NAME = RobotPathsNamingTest.class.getSimpleName();

    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    public static TemporaryFolder tempFolder = new TemporaryFolder();

    public static ResourceCreator resourceCreator = new ResourceCreator();

    @ClassRule
    public static TestRule rulesChain = RuleChain.outerRule(projectProvider).around(tempFolder).around(resourceCreator);

    private static final Function<IResource, List<String>> LAST_SEGMENT = r -> newArrayList(
            r.getLocation().removeFileExtension().lastSegment());

    private static final Function<IResource, List<String>> ALL_SEGMENTS = r -> newArrayList(
            r.getFullPath().removeFileExtension().segments());

    @BeforeClass
    public static void beforeSuite() throws Exception {
        final File nonWorkspaceFile = tempFolder.newFile("linked_suite.robot");
        resourceCreator.createLink(nonWorkspaceFile.toURI(), projectProvider.getFile("ProjectLink.robot"));
    }

    @Test
    public void testCreatingTopLevelSuiteName() throws Exception {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(RobotPathsNaming.createTopLevelSuiteName(newArrayList())).isEmpty();

            softly.assertThat(RobotPathsNaming.createTopLevelSuiteName(newArrayList(projectProvider.getProject())))
                    .isEmpty();

            softly.assertThat(RobotPathsNaming.createTopLevelSuiteName(
                    newArrayList(projectProvider.getProject(), projectProvider.getDir("Some Folder"))))
                    .isEqualTo(PROJECT_NAME + " & Some Folder");

            softly.assertThat(RobotPathsNaming.createTopLevelSuiteName(newArrayList(projectProvider.getProject(),
                    projectProvider.getDir("Some Folder"), projectProvider.getFile("Some Suite.robot"))))
                    .isEqualTo(PROJECT_NAME + " & Some Folder & Some Suite");

            softly.assertThat(RobotPathsNaming.createTopLevelSuiteName(
                    newArrayList(projectProvider.getProject(), projectProvider.getDir("underscored_folder"),
                            projectProvider.getFile("001__prefixed_suite.robot"))))
                    .isEqualTo(PROJECT_NAME + " & Underscored Folder & Prefixed Suite");

            softly.assertThat(RobotPathsNaming.createTopLevelSuiteName(
                    newArrayList(projectProvider.getProject(), projectProvider.getFile("ProjectLink.robot"))))
                    .isEqualTo(PROJECT_NAME + " & Linked Suite");
        });
    }

    @Test
    public void testCreatingSuiteNames() throws Exception {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(RobotPathsNaming.createSuiteNames(ImmutableMap.of(), "", LAST_SEGMENT)).isEmpty();

            softly.assertThat(RobotPathsNaming
                    .createSuiteNames(ImmutableMap.of(projectProvider.getProject(), newArrayList()), "", LAST_SEGMENT))
                    .containsExactly(PROJECT_NAME);

            softly.assertThat(RobotPathsNaming.createSuiteNames(
                    ImmutableMap.of(projectProvider.getFile("suite.robot"), newArrayList()), "", LAST_SEGMENT))
                    .containsExactly("Suite");

            softly.assertThat(RobotPathsNaming.createSuiteNames(
                    ImmutableMap.of(projectProvider.getFile("ProjectLink.robot"), newArrayList()), "", LAST_SEGMENT))
                    .containsExactly("Linked Suite");

            softly.assertThat(RobotPathsNaming.createSuiteNames(
                    ImmutableMap.of(projectProvider.getFile("suite.robot"), newArrayList("tc1", "tc2")), "",
                    LAST_SEGMENT)).containsExactly("Suite");

            softly.assertThat(RobotPathsNaming.createSuiteNames(
                    ImmutableMap.of(projectProvider.getFile("some/path/to/suite.robot"), newArrayList()), "",
                    ALL_SEGMENTS)).containsExactly(PROJECT_NAME + ".Some.Path.To.Suite");

            softly.assertThat(RobotPathsNaming.createSuiteNames(
                    ImmutableMap.of(projectProvider.getFile("some/path/to/suite.robot"), newArrayList()), "Top",
                    ALL_SEGMENTS)).containsExactly("Top." + PROJECT_NAME + ".Some.Path.To.Suite");

            softly.assertThat(
                    RobotPathsNaming.createSuiteNames(
                            ImmutableMap.of(projectProvider.getFile("very/very/long/path/to/suite.robot"),
                                    newArrayList(), projectProvider.getFile("another/path/to/test.robot"),
                                    newArrayList(), projectProvider.getDir("different/folder"), newArrayList()),
                            "TopName", ALL_SEGMENTS))
                    .containsExactly("TopName." + PROJECT_NAME + ".Very.Very.Long.Path.To.Suite",
                            "TopName." + PROJECT_NAME + ".Another.Path.To.Test",
                            "TopName." + PROJECT_NAME + ".Different.Folder");
        });
    }

    @Test
    public void testCreatingTestNames() throws Exception {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(RobotPathsNaming.createTestNames(ImmutableMap.of(), "", LAST_SEGMENT)).isEmpty();

            softly.assertThat(RobotPathsNaming.createTestNames(
                    ImmutableMap.of(projectProvider.getFile("suite.robot"), newArrayList("tc1", "tc2")), "",
                    LAST_SEGMENT)).containsExactly("Suite.tc1", "Suite.tc2");

            softly.assertThat(RobotPathsNaming.createTestNames(
                    ImmutableMap.of(projectProvider.getFile("ProjectLink.robot"), newArrayList("tc1", "tc2")), "",
                    LAST_SEGMENT)).containsExactly("Linked Suite.tc1", "Linked Suite.tc2");

            softly.assertThat(RobotPathsNaming.createTestNames(
                    ImmutableMap.of(projectProvider.getFile("test/suite.robot"), newArrayList("tc1", "tc2", "tc3")),
                    "TopName", ALL_SEGMENTS))
                    .containsExactly("TopName." + PROJECT_NAME + ".Test.Suite.tc1",
                            "TopName." + PROJECT_NAME + ".Test.Suite.tc2",
                            "TopName." + PROJECT_NAME + ".Test.Suite.tc3");

            softly.assertThat(RobotPathsNaming.createTestNames(
                    ImmutableMap.of(projectProvider.getFile("path/first.robot"), newArrayList("tc1"),
                            projectProvider.getFile("path/second.robot"), newArrayList("tc1", "tc2"),
                            projectProvider.getFile("path/third.robot"), newArrayList("tc1", "tc2", "tc3")),
                    "TopName", ALL_SEGMENTS))
                    .containsExactly("TopName." + PROJECT_NAME + ".Path.First.tc1",
                            "TopName." + PROJECT_NAME + ".Path.Second.tc1",
                            "TopName." + PROJECT_NAME + ".Path.Second.tc2",
                            "TopName." + PROJECT_NAME + ".Path.Third.tc1",
                            "TopName." + PROJECT_NAME + ".Path.Third.tc2",
                            "TopName." + PROJECT_NAME + ".Path.Third.tc3");
        });
    }

    @Test
    public void testCreatingSuiteName() throws Exception {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList())).isEmpty();

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList(""))).isEmpty();

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("a"))).isEqualTo("A");

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("a", "b", "c"))).isEqualTo("A.B.C");

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("abc"))).isEqualTo("Abc");

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("a_b_c"))).isEqualTo("A B C");

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("some path", "to suite")))
                    .isEqualTo("Some Path.To Suite");

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("some_path", "to_suite")))
                    .isEqualTo("Some Path.To Suite");

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("a", "001__b", "c")))
                    .isEqualTo("A.B.C");

            softly.assertThat(RobotPathsNaming.createSuiteName("", newArrayList("001__a__b_c", "001__d__e_f")))
                    .isEqualTo("A B C.D E F");

            softly.assertThat(RobotPathsNaming.createSuiteName(PROJECT_NAME, newArrayList("a", "b", "c")))
                    .isEqualTo(PROJECT_NAME + ".A.B.C");

            softly.assertThat(RobotPathsNaming.createSuiteName("Path1 & Path2", newArrayList("a", "b", "c")))
                    .isEqualTo("Path1 & Path2.A.B.C");
        });
    }

    @Test
    public void testCreatingTestName() throws Exception {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(RobotPathsNaming.createTestName("", newArrayList("path", "to", "suite"), "case"))
                    .isEqualTo("Path.To.Suite.case");

            softly.assertThat(RobotPathsNaming.createTestName(PROJECT_NAME, newArrayList("path", "to", "suite"),
                    "project test case")).isEqualTo(PROJECT_NAME + ".Path.To.Suite.project test case");

            softly.assertThat(RobotPathsNaming.createTestName(PROJECT_NAME, newArrayList("20__Some_path", "to_suite"),
                    "Test case")).isEqualTo(PROJECT_NAME + ".Some Path.To Suite.Test case");

            softly.assertThat(RobotPathsNaming.createTestName("Path1 & Path2", newArrayList("path", "to", "suite"),
                    "100__test_case")).isEqualTo("Path1 & Path2.Path.To.Suite.100__test_case");
        });
    }
}
