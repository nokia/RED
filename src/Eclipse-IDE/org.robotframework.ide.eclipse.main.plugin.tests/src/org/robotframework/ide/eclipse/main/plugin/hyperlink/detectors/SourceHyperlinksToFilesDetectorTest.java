/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.HyperlinksToFilesDetectorTest.objectsOfClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.FileHyperlink;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.CharStreams;

public class SourceHyperlinksToFilesDetectorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(SourceHyperlinksToFilesDetectorTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("file.robot", "");
        projectProvider.createFile("vars.py", "");
        projectProvider.createFile("lib.class", "");
        projectProvider.createFile("unhandled.txt", "");
        projectProvider.createDir("directory");
        projectProvider.createFile("directory/file.robot", "");
    }

    @Test
    public void noHyperlinksAreProvided_whenRegionsIsOutsideOfFile() throws Exception {
        final IFile file = projectProvider.createFile("f0.robot",
                "*** Settings ***",
                "Resource  file.robot  arg1  arg2");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final SourceHyperlinksToFilesDetector detector = new SourceHyperlinksToFilesDetector(suiteFile);
        assertThat(detector.detectHyperlinks(textViewer, new Region(-100, 1), true)).isNull();
        assertThat(detector.detectHyperlinks(textViewer, new Region(100, 1), true)).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenLibraryIsImportedUsingName() throws Exception {
        assertThat(detect("f1.robot", "Library", "lib_name")).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenUsingAbsolutePathWhichIsOutsideWorkspace()
            throws Exception {
        assertThat(detect("f2.robot", "Library", "/abs_path/to_file.py")).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenRelativePathPointsToDirectory() throws Exception {
        assertThat(detect("f3.robot", "Library", "directory/")).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenAbsolutePathPointsToDirectory() throws Exception {
        final String absPath = projectProvider.getProject().getLocation().append("directory/").toString();
        assertThat(detect("f4.robot", "Library", absPath)).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenRelativePathPointsNonExistingFile() throws Exception {
        assertThat(detect("f5.robot", "Resource", "directory/non_existing.robot")).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenAbsolutePathPointsNonExistingFile() throws Exception {
        final String absPath = projectProvider.getProject().getLocation()
                .append("directory/non_existing.robot").toString();
        assertThat(detect("f6.robot", "Resource", absPath)).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenRegionOutsidePathIsChosen() throws Exception {
        final IFile file = projectProvider.createFile("f7.robot",
                "*** Settings ***",
                "Resource  file.robot  arg1  arg2");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final SourceHyperlinksToFilesDetector detector = new SourceHyperlinksToFilesDetector(suiteFile);
        for (int i = 38; i < 49; i++) {
            assertThat(detector.detectHyperlinks(textViewer, new Region(i, 1), true)).isNull();
        }
    }

    @Test
    public void noHyperlinksAreProvided_forNonImportSetting() throws Exception {
        assertThat(detect("f8.robot", "Metadata", "file.robot")).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_forOtherElements() throws Exception {
        final IFile file = projectProvider.createFile("f9.robot",
                "*** Keywords ***",
                "kw",
                "  Log  file.robot");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final SourceHyperlinksToFilesDetector detector = new SourceHyperlinksToFilesDetector(suiteFile);
        assertThat(detector.detectHyperlinks(textViewer, new Region(30, 1), true)).isNull();
    }

    @Test
    public void fileHyperlinkIsProvided_whenPathPointsToExistingFile_1() throws Exception {
        final IHyperlink[] hyperlinks = detect("f10.robot", "Resource", "file.robot");
        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(FileHyperlink.class));
    }

    @Test
    public void fileHyperlinkIsProvided_whenPathPointsToExistingFile_2() throws Exception {
        final IHyperlink[] hyperlinks = detect("f11.robot", "Variables", "vars.py");
        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(FileHyperlink.class));
    }


    @Test
    public void fileHyperlinkIsProvided_whenPathPointsToExistingFile_3() throws Exception {
        final IHyperlink[] hyperlinks = detect("f12.robot", "Library", "lib.class");
        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(FileHyperlink.class));
    }

    private static IHyperlink[] detect(final String file, final String settingName, final String path)
            throws Exception {
        final IFile f = projectProvider.createFile(file, "*** Settings ***",
                settingName + "  " + path);
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(f);
        final Document document = new Document(getContent(f));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final SourceHyperlinksToFilesDetector detector = new SourceHyperlinksToFilesDetector(suiteFile);
        return detector.detectHyperlinks(textViewer, new Region(30, 1), true);
    }

    private static List<String> getContent(final IFile file) {
        try (InputStream stream = file.getContents()) {
            return Splitter.on('\n').splitToList(CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8)));
        } catch (IOException | CoreException e) {
            return new ArrayList<>();
        }
    }
}
