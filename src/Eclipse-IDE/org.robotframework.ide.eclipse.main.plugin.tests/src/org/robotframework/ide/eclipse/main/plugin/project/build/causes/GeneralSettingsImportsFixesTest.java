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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IMarkerResolution;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ChangeToFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RedSuiteMarkerResolution;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.junit.jupiter.RedTempDirectory;
import org.robotframework.red.junit.jupiter.StatefulProject;

@ExtendWith({ ProjectExtension.class, RedTempDirectory.class })
public class GeneralSettingsImportsFixesTest {

    @Project(dirs = { "Dir1", "Dir1/Dir2", "tests" },
            files = { "Lib.py", "Lib.java", "Res.robot", "Dir1/Lib.py", "Dir1/Dir2/Lib.py",
                    "tests/Lib.py", "tests/suite.robot" },
            cleanUpAfterEach = true)
    static StatefulProject project;

    @Project(name = "OTHER_PROJECT", files = { "Lib.py" })
    static IProject otherProject;

    @TempDir
    static File tempFolder;

    private static IMarker marker;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        marker = project.getFile("tests/suite.robot").createMarker(RedPlugin.PLUGIN_ID);
    }

    @BeforeEach
    public void beforeTest() throws Exception {
        project.configure();
    }

    @AfterEach
    public void afterTest() throws Exception {
        project.deconfigure();
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
        config.setReferencedLibraries(
                newArrayList(ReferencedLibrary.create(LibraryType.PYTHON, "LibFromConfig", project.getName())));
        project.configure(config);

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
        final File nonWorkspaceFolder = RedTempDirectory.createNewDir(tempFolder, "linked");
        final File nonWorkspaceFile = RedTempDirectory.createNewFile(tempFolder, "linked/LinkedLib.py");
        project.createDirLink("linked", nonWorkspaceFolder.toURI());
        project.createFileLink("LinkedLib.py", nonWorkspaceFile.toURI());

        final List<RedSuiteMarkerResolution> fixers = GeneralSettingsImportsFixes
                .changeByPathImportToOtherPathWithSameFileName(marker, new Path(nonWorkspaceFile.getAbsolutePath()));

        assertThat(fixers).isEmpty();
    }
}
