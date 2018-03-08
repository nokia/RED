/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.project.build.fix.Fixers.byApplyingToDocument;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IMarkerResolution;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RedSuiteMarkerResolution;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Splitter;


public class GeneralSettingsImportsFixesTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(GeneralSettingsImportsFixesTest.class);

    @ClassRule
    public static ProjectProvider otherProjectProvider = new ProjectProvider("OTHER_PROJECT");

    private static IFile suite;

    private static IMarker marker;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        final String projectPath = projectProvider.getProject().getName();

        final List<ReferencedLibrary> libs = newArrayList(
                ReferencedLibrary.create(LibraryType.PYTHON, "Lib", projectPath),
                ReferencedLibrary.create(LibraryType.PYTHON, "OtherLibName", projectPath),
                ReferencedLibrary.create(LibraryType.PYTHON, "Different", projectPath));
        
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setLibraries(libs);
        projectProvider.configure(config);

        projectProvider.createFile("Lib.py");
        projectProvider.createFile("Lib.java");
        projectProvider.createFile("Res.robot");

        projectProvider.createDir("Dir1");
        projectProvider.createFile("Dir1/Lib.py");
        projectProvider.createDir("Dir1/Dir2");
        projectProvider.createFile("Dir1/Dir2/Lib.py");

        projectProvider.createDir("tests");
        projectProvider.createFile("tests/Lib.py");

        suite = projectProvider.createFile("tests/suite.robot", "*** Settings ***", "Library  ../../Lib.py",
                "*** Test Cases ***");

        otherProjectProvider.createFile("Lib.py");

        marker = suite.createMarker(RedPlugin.PLUGIN_ID);
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "../../Lib.py");
        marker.setAttribute(IMarker.CHAR_START, 26);
        marker.setAttribute(IMarker.CHAR_END, 38);
    }

    @Test
    public void thereAreNoFixersForByNameImports_whenNoLibraryIsKnownForGivenPath() throws Exception {
        final List<RedSuiteMarkerResolution> fixers = GeneralSettingsImportsFixes.changeByPathImportToByName(marker,
                new Path("Other.py"));

        assertThat(fixers).isEmpty();
    }

    @Test
    public void thereIsAFixersForByNameImports_whenThereIsLibraryKnownForGivenPath() throws Exception {
        final List<RedSuiteMarkerResolution> fixers = GeneralSettingsImportsFixes.changeByPathImportToByName(marker,
                new Path(marker.getAttribute(AdditionalMarkerAttributes.PATH).toString()));

        assertThat(fixers.stream().map(IMarkerResolution::getLabel)).containsExactly("Change to 'Lib'");

        final IDocument document = new Document(Splitter.on('\n').splitToList(projectProvider.getFileContent(suite)));
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);
        assertThat(fixers.stream().map(byApplyingToDocument(marker, document, model)))
                .containsExactly(new Document("*** Settings ***", "Library  Lib", "*** Test Cases ***"));
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

        assertThat(fixers.stream().map(IMarkerResolution::getLabel)).containsExactly("Change to '../Dir1/Dir2/Lib.py'",
                "Change to '../Dir1/Lib.py'", "Change to '../Lib.py'", "Change to '../../OTHER_PROJECT/Lib.py'");

        final IDocument document = new Document(Splitter.on('\n').splitToList(projectProvider.getFileContent(suite)));
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);
        assertThat(fixers.stream().map(byApplyingToDocument(marker, document, model))).containsExactly(
                new Document("*** Settings ***", "Library  ../Dir1/Dir2/Lib.py", "*** Test Cases ***"),
                new Document("*** Settings ***", "Library  ../Dir1/Lib.py", "*** Test Cases ***"),
                new Document("*** Settings ***", "Library  ../Lib.py", "*** Test Cases ***"),
                new Document("*** Settings ***", "Library  ../../OTHER_PROJECT/Lib.py", "*** Test Cases ***"));
    }

    @Test
    public void thereAreFixersForOtherPathImports_whenOtherFilesWithSameNameExist_2() throws Exception {
        final List<RedSuiteMarkerResolution> fixers = GeneralSettingsImportsFixes
                .changeByPathImportToOtherPathWithSameFileName(marker,
                        new Path(marker.getAttribute(AdditionalMarkerAttributes.PATH).toString()));

        assertThat(fixers.stream().map(IMarkerResolution::getLabel)).containsExactly("Change to '../Dir1/Dir2/Lib.py'",
                "Change to '../Dir1/Lib.py'", "Change to '../Lib.py'", "Change to 'Lib.py'",
                "Change to '../../OTHER_PROJECT/Lib.py'");

        final IDocument document = new Document(Splitter.on('\n').splitToList(projectProvider.getFileContent(suite)));
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);
        assertThat(fixers.stream().map(byApplyingToDocument(marker, document, model))).containsExactly(
                new Document("*** Settings ***", "Library  ../Dir1/Dir2/Lib.py", "*** Test Cases ***"),
                new Document("*** Settings ***", "Library  ../Dir1/Lib.py", "*** Test Cases ***"),
                new Document("*** Settings ***", "Library  ../Lib.py", "*** Test Cases ***"),
                new Document("*** Settings ***", "Library  Lib.py", "*** Test Cases ***"),
                new Document("*** Settings ***", "Library  ../../OTHER_PROJECT/Lib.py", "*** Test Cases ***"));
    }
}
