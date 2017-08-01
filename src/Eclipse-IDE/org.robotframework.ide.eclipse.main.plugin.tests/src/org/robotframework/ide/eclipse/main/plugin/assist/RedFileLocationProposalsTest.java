/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.prefixesMatcher;

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

    private static RobotSuiteFile suiteFile;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("dir1");
        projectProvider.createDir("dir2");

        projectProvider.createFile("dir1/res1.robot", "*** Variables ***");
        projectProvider.createFile("dir1/lib1.py");
        projectProvider.createFile("dir1/vars1.py");
        projectProvider.createFile("dir2/lib2.py");
        projectProvider.createFile("dir2/vars2.py");
        projectProvider.createFile("dir2/res2.robot", "*** Variables ***");
        projectProvider.createFile("dir2/tests.robot", "*** Test Cases ***");

        final IFile file = projectProvider.createFile("importing_file.robot", "*** Test Cases ***");
        suiteFile = new RobotModel().createSuiteFile(file);
    }

    @AfterClass
    public static void afterSuite() {
        suiteFile.dispose();
        suiteFile = null;
    }

    @Test
    public void exceptionIsThrown_whenTryingToCreateProposalsProviderForUnsupportedSettingsGroup() {
        final EnumSet<SettingsGroup> supportedSettingGroups = EnumSet.of(SettingsGroup.RESOURCES,
                SettingsGroup.VARIABLES, SettingsGroup.LIBRARIES);

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
    public void noResourcesProposalsProvided_whenNoResourceIsMatchingToGivenInput() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.RESOURCES,
                suiteFile);

        assertThat(proposalsProvider.getFilesLocationsProposals("unknown")).isEmpty();
    }

    @Test
    public void onlyResourcesProposalsContainingInputAreProvided_whenDefaultMatcherIsUsed() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.RESOURCES,
                suiteFile);

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("1");
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("dir1/res1.robot");
    }

    @Test
    public void onlyResourcesProposalsMatchingInputAreProvided_whenCustomMatcherIsUsed() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.RESOURCES,
                suiteFile, prefixesMatcher());

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("dir1");
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("dir1/res1.robot");
    }

    @Test
    public void allResourcesProposalsAreProvided_whenInputIsEmpty() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.RESOURCES,
                suiteFile);

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("");
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("dir1/res1.robot",
                "dir2/res2.robot");
    }

    @Test
    public void allResourcesProposalsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.RESOURCES,
                suiteFile);

        final Comparator<IFile> comparator = firstFileNameContaining("2");
        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("", comparator);
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("dir2/res2.robot",
                "dir1/res1.robot");
    }

    @Test
    public void noVariablesFilesProposalsProvided_whenNoResourceIsMatchingToGivenInput() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.VARIABLES,
                suiteFile);

        assertThat(proposalsProvider.getFilesLocationsProposals("unknown")).isEmpty();
    }

    @Test
    public void onlyVariablesFilesProposalsContainingInputAreProvided_whenDefaultMatcherIsUsed() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.VARIABLES,
                suiteFile);

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("vars");
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("dir1/vars1.py", "dir2/vars2.py");
    }

    @Test
    public void onlyVariablesFilesProposalsMatchingInputAreProvided_whenCustomMatcherIsUsed() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.VARIABLES,
                suiteFile, prefixesMatcher());

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("dir1/");
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("dir1/lib1.py", "dir1/vars1.py");
    }

    @Test
    public void allVariablesFilesProposalsAreProvided_whenInputIsEmpty() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.VARIABLES,
                suiteFile);

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("");
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("dir1/lib1.py", "dir1/vars1.py",
                "dir2/lib2.py", "dir2/vars2.py");
    }

    @Test
    public void allVariablesFilesProposalsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.VARIABLES,
                suiteFile);

        final Comparator<IFile> comparator = firstFileNameContaining("vars");
        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("2", comparator);
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("dir2/vars2.py", "dir2/lib2.py");
    }

    @Test
    public void noLibrariesFilesProposalsProvided_whenNoResourceIsMatchingToInput() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.LIBRARIES,
                suiteFile);

        assertThat(proposalsProvider.getFilesLocationsProposals("unknown")).isEmpty();
    }

    @Test
    public void onlyLibrariesFilesProposalsMatchingInputAreProvided_whenDefaultMatcherIsUsed() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.LIBRARIES,
                suiteFile);

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("lib");
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("dir1/lib1.py", "dir2/lib2.py");
    }

    @Test
    public void onlyLibrariesFilesProposalsMatchingInputAreProvided_whenCustomMatcherIsUsed() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.LIBRARIES,
                suiteFile, prefixesMatcher());

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("dir2/");
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("dir2/lib2.py", "dir2/vars2.py");
    }

    @Test
    public void allLibrariesFilesProposalsAreProvided_whenInputIsEmpty() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.LIBRARIES,
                suiteFile);

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("");
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("dir1/lib1.py", "dir1/vars1.py",
                "dir2/lib2.py", "dir2/vars2.py");
    }

    @Test
    public void allLibrariesFilesProposalsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.LIBRARIES,
                suiteFile);

        final Comparator<IFile> comparator = firstFileNameContaining("vars");
        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("1", comparator);
        assertThat(transform(proposals, AssistProposal::getLabel)).containsExactly("dir1/vars1.py", "dir1/lib1.py");
    }

    private static Comparator<IFile> firstFileNameContaining(final String toContain) {
        return new Comparator<IFile>() {

            @Override
            public int compare(final IFile o1, final IFile o2) {

                final boolean contains1 = o1.getName().contains(toContain);
                final boolean contains2 = o2.getName().contains(toContain);
                final int result = Boolean.compare(contains2, contains1);
                if (result != 0) {
                    return result;
                }
                return Integer.compare(o1.getName().indexOf(toContain), o2.getName().indexOf(toContain));
            }
        };
    }
}
