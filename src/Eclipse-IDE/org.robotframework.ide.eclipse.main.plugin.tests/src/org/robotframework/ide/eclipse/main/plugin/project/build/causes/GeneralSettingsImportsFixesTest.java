/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IMarkerResolution;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ChangeToFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RedSuiteMarkerResolution;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ResourceCreator;

public class GeneralSettingsImportsFixesTest {

    public static ProjectProvider projectProvider = new ProjectProvider(GeneralSettingsImportsFixesTest.class);

    public static TemporaryFolder tempFolder = new TemporaryFolder();

    @ClassRule
    public static TestRule rulesChain = RuleChain.outerRule(projectProvider).around(tempFolder);

    @ClassRule
    public static ProjectProvider otherProjectProvider = new ProjectProvider("OTHER_PROJECT");

    @Rule
    public ResourceCreator resourceCreator = new ResourceCreator();

    private static IMarker marker;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("Lib.py");
        projectProvider.createFile("Lib.java");
        projectProvider.createFile("Res.robot");

        projectProvider.createDir("Dir1");
        projectProvider.createFile("Dir1/Lib.py");
        projectProvider.createDir("Dir1/Dir2");
        projectProvider.createFile("Dir1/Dir2/Lib.py");

        projectProvider.createDir("tests");
        projectProvider.createFile("tests/Lib.py");

        otherProjectProvider.createFile("Lib.py");

        marker = projectProvider.createFile("tests/suite.robot").createMarker(RedPlugin.PLUGIN_ID);
    }

    @Before
    public void beforeTest() throws Exception {
        projectProvider.configure();
    }

    @After
    public void afterTest() throws Exception {
        projectProvider.deconfigure();
    }

    @Test
    public void thereAreNoFixersForByNameImports_whenNoLibraryIsKnownForGivenPath() throws Exception {
        final List<RedSuiteMarkerResolution> fixers = GeneralSettingsImportsFixes.changeByPathImportToByName(marker,
                new Path("LibNotFromConfig.py"));

        assertThat(fixers).isEmpty();
    }

    @Test
    public void thereIsAFixersForByNameImports_whenThereIsLibraryKnownForGivenPath() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setLibraries(newArrayList(
                ReferencedLibrary.create(LibraryType.PYTHON, "LibFromConfig", projectProvider.getProject().getName())));
        projectProvider.configure(config);

        final List<RedSuiteMarkerResolution> fixers = GeneralSettingsImportsFixes.changeByPathImportToByName(marker,
                new Path("../../LibFromConfig.py"));

        assertThat(fixers).allMatch(fixer -> fixer instanceof ChangeToFixer);
        assertThat(fixers.stream().map(IMarkerResolution::getLabel)).containsExactly("Change to 'LibFromConfig'");
    }

    @Test
    public void thereAreNoFixersForOtherPathImports_whenNoOtherFileWithSameNameExist() throws Exception {
        final List<RedSuiteMarkerResolution> fixers = GeneralSettingsImportsFixes
                .changeByPathImportToOtherPathWithSameFileName(marker, new Path("Other.py"));

        assertThat(fixers).isEmpty();
    }

    @Test
    public void thereAreFixersForOtherPathImports_whenOtherFilesWithSameNameExist_1() throws Exception {
        final List<RedSuiteMarkerResolution> fixers = GeneralSettingsImportsFixes
                .changeByPathImportToOtherPathWithSameFileName(marker, new Path("Lib.py"));

        assertThat(fixers).allMatch(fixer -> fixer instanceof ChangeToFixer);
        assertThat(fixers.stream().map(IMarkerResolution::getLabel)).containsExactly("Change to '../Dir1/Dir2/Lib.py'",
                "Change to '../Dir1/Lib.py'", "Change to '../Lib.py'", "Change to '../../OTHER_PROJECT/Lib.py'");
    }

    @Test
    public void thereAreFixersForOtherPathImports_whenOtherFilesWithSameNameExist_2() throws Exception {
        final List<RedSuiteMarkerResolution> fixers = GeneralSettingsImportsFixes
                .changeByPathImportToOtherPathWithSameFileName(marker, new Path("../../Lib.py"));

        assertThat(fixers).allMatch(fixer -> fixer instanceof ChangeToFixer);
        assertThat(fixers.stream().map(IMarkerResolution::getLabel)).containsExactly("Change to '../Dir1/Dir2/Lib.py'",
                "Change to '../Dir1/Lib.py'", "Change to '../Lib.py'", "Change to 'Lib.py'",
                "Change to '../../OTHER_PROJECT/Lib.py'");
    }

    @Test
    public void thereAreNoFixersForOtherPathImports_whenOtherFilesWithSameNameAreLinked() throws Exception {
        final File nonWorkspaceFolder = tempFolder.newFolder("linked");
        final File nonWorkspaceFile = tempFolder.newFile("linked/LinkedLib.py");
        resourceCreator.createLink(nonWorkspaceFolder.toURI(), projectProvider.getDir("linked"));
        resourceCreator.createLink(nonWorkspaceFile.toURI(), projectProvider.getFile("LinkedLib.py"));

        final List<RedSuiteMarkerResolution> fixers = GeneralSettingsImportsFixes
                .changeByPathImportToOtherPathWithSameFileName(marker, new Path(nonWorkspaceFile.getAbsolutePath()));

        assertThat(fixers).isEmpty();
    }
}
