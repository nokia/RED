/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.prefixesMatcher;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class RedFileLocationProposalsTest {

    @Project(dirs = { "dir1", "dir2" }, files = { "dir1/res1.robot", "dir1/lib1.py", "dir1/vars1.py", "dir1/vars1.yml",
            "dir2/res2.robot", "dir2/lib2.py", "dir2/vars2.py", "dir2/vars2.yaml" })
    static IProject project;

    private static RobotSuiteFile suiteFile;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        createFile(project, "dir2/tests.robot", "*** Test Cases ***");

        suiteFile = new RobotModel().createSuiteFile(createFile(project, "importing_file.robot", "*** Test Cases ***"));
    }

    @AfterAll
    public static void afterSuite() {
        suiteFile.dispose();
        suiteFile = null;
    }

    @Test
    public void exceptionIsThrown_whenTryingToCreateProposalsProviderForUnsupportedSettingsGroup() {
        final EnumSet<SettingsGroup> supportedSettingGroups = EnumSet.of(SettingsGroup.RESOURCES,
                SettingsGroup.VARIABLES, SettingsGroup.LIBRARIES);

        for (final SettingsGroup importType : EnumSet.complementOf(supportedSettingGroups)) {
            assertThatIllegalStateException()
                    .isThrownBy(() -> RedFileLocationProposals.create(importType, new RobotSuiteFileCreator().build()))
                    .withNoCause();
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
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("dir1/res1.robot");
    }

    @Test
    public void onlyResourcesProposalsMatchingInputAreProvided_whenCustomMatcherIsUsed() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.RESOURCES,
                suiteFile, prefixesMatcher());

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("dir1");
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("dir1/res1.robot");
    }

    @Test
    public void allResourcesProposalsAreProvided_whenInputIsEmpty() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.RESOURCES,
                suiteFile);

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("");
        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("dir1/res1.robot", "dir2/res2.robot");
    }

    @Test
    public void allResourcesProposalsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.RESOURCES,
                suiteFile);

        final Comparator<IFile> comparator = firstFileNameContaining("2");
        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("", comparator);
        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("dir2/res2.robot", "dir1/res1.robot");
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
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("dir1/vars1.py", "dir1/vars1.yml",
                "dir2/vars2.py", "dir2/vars2.yaml");
    }

    @Test
    public void onlyVariablesFilesProposalsMatchingInputAreProvided_whenCustomMatcherIsUsed() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.VARIABLES,
                suiteFile, prefixesMatcher());

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("dir1/");
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("dir1/lib1.py", "dir1/vars1.py",
                "dir1/vars1.yml");
    }

    @Test
    public void allVariablesFilesProposalsAreProvided_whenInputIsEmpty() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.VARIABLES,
                suiteFile);

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("");
        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("dir1/lib1.py", "dir1/vars1.py", "dir1/vars1.yml", "dir2/lib2.py", "dir2/vars2.py",
                        "dir2/vars2.yaml");
    }

    @Test
    public void allVariablesFilesProposalsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.VARIABLES,
                suiteFile);

        final Comparator<IFile> comparator = firstFileNameContaining("vars");
        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("2", comparator);
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("dir2/vars2.py", "dir2/vars2.yaml",
                "dir2/lib2.py");
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
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("dir1/lib1.py", "dir2/lib2.py");
    }

    @Test
    public void onlyLibrariesFilesProposalsMatchingInputAreProvided_whenCustomMatcherIsUsed() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.LIBRARIES,
                suiteFile, prefixesMatcher());

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("dir2/");
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("dir2/lib2.py", "dir2/vars2.py");
    }

    @Test
    public void allLibrariesFilesProposalsAreProvided_whenInputIsEmpty() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.LIBRARIES,
                suiteFile);

        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("");
        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("dir1/lib1.py", "dir1/vars1.py",
                "dir2/lib2.py", "dir2/vars2.py");
    }

    @Test
    public void allLibrariesFilesProposalsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided() {
        final RedFileLocationProposals proposalsProvider = RedFileLocationProposals.create(SettingsGroup.LIBRARIES,
                suiteFile);

        final Comparator<IFile> comparator = firstFileNameContaining("vars");
        final List<? extends AssistProposal> proposals = proposalsProvider.getFilesLocationsProposals("1", comparator);
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("dir1/vars1.py", "dir1/lib1.py");
    }

    private static Comparator<IFile> firstFileNameContaining(final String toContain) {
        return (f1, f2) -> {
            final boolean contains1 = f1.getName().contains(toContain);
            final boolean contains2 = f2.getName().contains(toContain);
            final int result = Boolean.compare(contains2, contains1);
            if (result != 0) {
                return result;
            }
            return Integer.compare(f1.getName().indexOf(toContain), f2.getName().indexOf(toContain));
        };
    }
}
