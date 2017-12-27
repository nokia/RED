/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.project.build.fix.Fixers.byApplyingToDocument;

import java.util.List;
import java.util.function.Function;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IMarkerResolution;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Splitter;

public class ChangeImportedPathFixerTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ChangeImportedPathFixerTest.class);

    @ClassRule
    public static ProjectProvider otherProjectProvider = new ProjectProvider("OTHER_PROJECT");

    private static IFile suite;

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

        otherProjectProvider.createFile("Lib.py");

        projectProvider.createDir("tests");
        projectProvider.createFile("tests/Lib.py");

        suite = projectProvider.createFile("tests/suite.robot", "*** Settings ***", "Library  ../../Lib.py",
                "*** Test Cases ***");

        marker = suite.createMarker(RedPlugin.PLUGIN_ID);
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "../../Lib.py");
        marker.setAttribute(IMarker.CHAR_START, 26);
        marker.setAttribute(IMarker.CHAR_END, 38);
    }

    @Test
    public void testCreatingEmptyFixers() throws Exception {
        final List<ChangeImportedPathFixer> fixers = ChangeImportedPathFixer.createFixersForSameFile(suite,
                new Path("Other.py"));

        assertThat(fixers).isEmpty();
    }

    @Test
    public void testEmptyFixersForSamePath() throws Exception {
        final List<ChangeImportedPathFixer> fixers = ChangeImportedPathFixer.createFixersForSameFile(suite,
                new Path("Lib.py"));

        assertThat(fixers.stream().map(IMarkerResolution::getLabel)).containsExactly("Change to ../Dir1/Dir2/Lib.py",
                "Change to ../Dir1/Lib.py", "Change to ../Lib.py", "Change to ../../OTHER_PROJECT/Lib.py");
    }

    @Test
    public void testCreatingNotEmptyFixers() throws Exception {
        final List<ChangeImportedPathFixer> fixers = ChangeImportedPathFixer.createFixersForSameFile(suite,
                new Path(marker.getAttribute(AdditionalMarkerAttributes.PATH).toString()));

        assertThat(fixers.stream().map(IMarkerResolution::getLabel)).containsExactly("Change to ../Dir1/Dir2/Lib.py",
                "Change to ../Dir1/Lib.py", "Change to ../Lib.py", "Change to Lib.py",
                "Change to ../../OTHER_PROJECT/Lib.py");
    }

    @Test
    public void testApplyingFixers() throws Exception {
        final List<ChangeImportedPathFixer> fixers = ChangeImportedPathFixer.createFixersForSameFile(suite,
                new Path(marker.getAttribute(AdditionalMarkerAttributes.PATH).toString()));

        final IDocument document = new Document(Splitter.on('\n').splitToList(projectProvider.getFileContent(suite)));
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);
        final Function<RedSuiteMarkerResolution, IDocument> mapper = byApplyingToDocument(marker, document, model);
        assertThat(fixers.stream().map(mapper::apply)).containsExactly(
                new Document("*** Settings ***", "Library  ../Dir1/Dir2/Lib.py", "*** Test Cases ***"),
                new Document("*** Settings ***", "Library  ../Dir1/Lib.py", "*** Test Cases ***"),
                new Document("*** Settings ***", "Library  ../Lib.py", "*** Test Cases ***"),
                new Document("*** Settings ***", "Library  Lib.py", "*** Test Cases ***"),
                new Document("*** Settings ***", "Library  ../../OTHER_PROJECT/Lib.py", "*** Test Cases ***"));
    }
}
