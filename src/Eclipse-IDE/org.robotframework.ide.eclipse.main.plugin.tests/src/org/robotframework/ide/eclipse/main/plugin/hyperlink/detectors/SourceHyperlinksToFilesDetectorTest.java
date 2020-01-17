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
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFileContent;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.FileHyperlink;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class SourceHyperlinksToFilesDetectorTest {

    @Project(dirs = { "directory" }, files = { "file.robot", "vars.py", "lib.class", "unhandled.txt",
            "directory/file.robot" })
    static IProject project;

    @Test
    public void noHyperlinksAreProvided_whenRegionsIsOutsideOfFile() throws Exception {
        final IFile file = createFile(project, "f0.robot",
                "*** Settings ***",
                "Resource  file.robot  arg1  arg2");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final Document document = new Document(getFileContent(file));

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
        final String absPath = project.getLocation().append("directory/").toString();
        assertThat(detect("f4.robot", "Library", absPath)).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenRelativePathPointsNonExistingFile() throws Exception {
        assertThat(detect("f5.robot", "Resource", "directory/non_existing.robot")).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenAbsolutePathPointsNonExistingFile() throws Exception {
        final String absPath = project.getLocation()
                .append("directory/non_existing.robot").toString();
        assertThat(detect("f6.robot", "Resource", absPath)).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenRegionOutsidePathIsChosen() throws Exception {
        final IFile file = createFile(project, "f7.robot",
                "*** Settings ***",
                "Resource  file.robot  arg1  arg2");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final Document document = new Document(getFileContent(file));

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
        final IFile file = createFile(project, "f9.robot",
                "*** Keywords ***",
                "kw",
                "  Log  file.robot");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final Document document = new Document(getFileContent(file));

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

    private static IHyperlink[] detect(final String filePath, final String settingName, final String path)
            throws Exception {
        final IFile file = createFile(project, filePath, "*** Settings ***",
                settingName + "  " + path);
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final Document document = new Document(getFileContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final SourceHyperlinksToFilesDetector detector = new SourceHyperlinksToFilesDetector(suiteFile);
        return detector.detectHyperlinks(textViewer, new Region(30, 1), true);
    }
}
