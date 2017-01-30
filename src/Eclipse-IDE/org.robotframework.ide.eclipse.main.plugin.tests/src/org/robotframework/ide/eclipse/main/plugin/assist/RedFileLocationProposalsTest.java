/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.substringMatcher;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.toLabels;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

public class RedFileLocationProposalsTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RedFileLocationProposalsTest.class);

    private static IFile importingFile;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("dir1");
        projectProvider.createDir("dir1_1");
        projectProvider.createDir("dir2");

        importingFile = projectProvider.createFile("importing_file.robot", "*** Test Cases ***");
        projectProvider.createFile("dir1/res1.robot", "*** Variables ***");
        projectProvider.createFile("dir1_1/lib.py");
        projectProvider.createFile("dir1_1/vars.py");
        projectProvider.createFile("dir2/res2.robot", "*** Variables ***");
        projectProvider.createFile("dir2/tests.robot", "*** Test Cases ***");
    }

    @AfterClass
    public static void afterSuite() {
        importingFile = null;
    }

    @Test
    public void exceptionIsThrown_whenTryingToCreateProposalsProviderForUnsupportedSettingsGroup() {
        final EnumSet<SettingsGroup> supportedSettingGroups = EnumSet.of(SettingsGroup.RESOURCES, SettingsGroup.VARIABLES);

        for (final SettingsGroup importType : EnumSet.complementOf(supportedSettingGroups)) {
            try {
                RedFileLocationProposals.create(importType, new RobotSuiteFileCreator().build());
                fail("It should not be possible to create file locations proposals for " + importType);
            } catch (final IllegalStateException e) {
                // that's what is expected here
            }
        }
    }

    @Test
    public void noResourcesProposalsProvided_whenNoResourceIsMatchingToGivenPrefix() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.RESOURCES,
                model);

        assertThat(proposalsProvider.getFilesLocationsProposals("unknown")).isEmpty();
    }

    @Test
    public void onlyResourcesProposalsMatchingPrefixAreProvided_whenPrefixIsGivenAndDefaultMatcherIsUsed() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.RESOURCES,
                model);

        final List<? extends AssistProposal> proposals = proposalsProvider
                .getFilesLocationsProposals(RedFileLocationProposalsTest.class.getSimpleName() + "/dir1");
        assertThat(transform(proposals, toLabels()))
                .containsExactly("dir1/res1.robot");
    }

    @Test
    public void onlyResourcesProposalsMatchingInputAreProvided_whenCustomMatcherIsUsed() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.RESOURCES,
                model, substringMatcher());

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("1");
        assertThat(transform(proposals, toLabels()))
                .containsExactly("dir1/res1.robot");
    }

    @Test
    public void allResourcesProposalsAreProvided_whenPrefixIsEmptyAndDefaultMatcherIsUsed() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.RESOURCES,
                model);

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("");
        assertThat(transform(proposals, toLabels())).containsExactly(
                "dir1/res1.robot", "dir2/res2.robot");
    }

    @Test
    public void allResourcesProposalsAreProvidedInOrderInducedByComparator_whenCustomComparatorIsUsed() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.RESOURCES,
                model);

        final Comparator<IFile> comparator = new Comparator<IFile>() {

            @Override
            public int compare(final IFile o1, final IFile o2) {
                if (o1.equals(o2)) {
                    return 0;
                } else if (o1.getName().contains("2")) {
                    return -1;
                } else {
                    return 1;
                }
            }
        };
        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("", comparator);
        assertThat(transform(proposals, toLabels())).containsExactly("dir2/res2.robot", "dir1/res1.robot");
    }

    @Test
    public void noVariablesFilesProposalsProvided_whenNoResourceIsMatchingToGivenPrefix() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.VARIABLES,
                model);

        assertThat(proposalsProvider.getFilesLocationsProposals("unknown")).isEmpty();
    }

    @Test
    public void onlyVariablesFilesProposalsMatchingPrefixAreProvided_whenPrefixIsGivenAndDefaultMatcherIsUsed() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.VARIABLES,
                model);

        final List<? extends AssistProposal> proposals = proposalsProvider
                .getFilesLocationsProposals(RedFileLocationProposalsTest.class.getSimpleName() + "/dir1_1/l");
        assertThat(transform(proposals, toLabels()))
                .containsExactly("dir1_1/lib.py");
    }

    @Test
    public void onlyVariablesFilesProposalsMatchingInputAreProvided_whenCustomMatcherIsUsed() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.VARIABLES,
                model, substringMatcher());

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("vars");
        assertThat(transform(proposals, toLabels()))
                .containsExactly("dir1_1/vars.py");
    }

    @Test
    public void allVariablesFilesProposalsAreProvided_whenPrefixIsEmptyAndDefaultMatcherIsUsed() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.VARIABLES,
                model);

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("");
        assertThat(transform(proposals, toLabels())).containsExactly(
                "dir1_1/lib.py", "dir1_1/vars.py");
    }

    @Test
    public void allVariablesFilesProposalsAreProvidedInOrderInducedByComparator_whenCustomComparatorIsUsed() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.VARIABLES,
                model);

        final Comparator<IFile> comparator = new Comparator<IFile>() {

            @Override
            public int compare(final IFile o1, final IFile o2) {
                if (o1.equals(o2)) {
                    return 0;
                } else if (o1.getName().contains("vars")) {
                    return -1;
                } else {
                    return 1;
                }
            }
        };
        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("", comparator);
        assertThat(transform(proposals, toLabels())).containsExactly(
                "dir1_1/vars.py", "dir1_1/lib.py");
    }
}
