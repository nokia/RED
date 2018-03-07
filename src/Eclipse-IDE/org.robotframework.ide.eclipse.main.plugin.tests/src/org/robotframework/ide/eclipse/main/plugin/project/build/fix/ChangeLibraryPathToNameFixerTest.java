/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static com.google.common.collect.Lists.newArrayList;
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
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Splitter;

public class ChangeLibraryPathToNameFixerTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ChangeLibraryPathToNameFixerTest.class);

    private static IFile suite;

    private static IMarker marker;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setLibraries(newArrayList(ReferencedLibrary.create(LibraryType.PYTHON, "LibName", ""),
                ReferencedLibrary.create(LibraryType.PYTHON, "OtherLibName", ""),
                ReferencedLibrary.create(LibraryType.PYTHON, "Different", "")));
        projectProvider.configure(config);

        suite = projectProvider.createFile("suite.robot", "*** Settings ***", "Library  ../../LibName.py",
                "*** Test Cases ***");

        marker = suite.createMarker(RedPlugin.PLUGIN_ID);
        marker.setAttribute(AdditionalMarkerAttributes.PATH, "../../LibName.py");
        marker.setAttribute(IMarker.CHAR_START, 26);
        marker.setAttribute(IMarker.CHAR_END, 42);
    }

    @Test
    public void testCreatingEmptyFixers() throws Exception {
        final List<ChangeLibraryPathToNameFixer> fixers = ChangeLibraryPathToNameFixer.createFixersForSameFile(suite,
                new Path("Other.py"));

        assertThat(fixers).isEmpty();
    }

    @Test
    public void testCreatingNotEmptyFixers() throws Exception {
        final List<ChangeLibraryPathToNameFixer> fixers = ChangeLibraryPathToNameFixer.createFixersForSameFile(suite,
                new Path(marker.getAttribute(AdditionalMarkerAttributes.PATH).toString()));

        assertThat(fixers.stream().map(IMarkerResolution::getLabel)).containsExactly("Change to LibName");
    }

    @Test
    public void testApplyingFixers() throws Exception {
        final List<ChangeLibraryPathToNameFixer> fixers = ChangeLibraryPathToNameFixer.createFixersForSameFile(suite,
                new Path(marker.getAttribute(AdditionalMarkerAttributes.PATH).toString()));

        final IDocument document = new Document(Splitter.on('\n').splitToList(projectProvider.getFileContent(suite)));
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);
        final Function<RedSuiteMarkerResolution, IDocument> mapper = byApplyingToDocument(marker, document, model);
        assertThat(fixers.stream().map(mapper::apply))
                .containsExactly(new Document("*** Settings ***", "Library  LibName", "*** Test Cases ***"));
    }
}
